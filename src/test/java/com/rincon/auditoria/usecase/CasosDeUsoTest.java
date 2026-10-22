package com.rincon.auditoria.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rincon.auditoria.domain.exception.HallazgoNoEncontradoException;
import com.rincon.auditoria.domain.exception.TransicionInvalidaException;
import com.rincon.auditoria.domain.model.CambioEstado;
import com.rincon.auditoria.domain.model.EstadoHallazgo;
import com.rincon.auditoria.domain.model.HallazgoAuditoria;
import com.rincon.auditoria.domain.model.HallazgoId;
import com.rincon.auditoria.domain.model.Severidad;
import com.rincon.auditoria.soporte.Fakes.HistorialEnMemoria;
import com.rincon.auditoria.soporte.Fakes.RelojMutable;
import com.rincon.auditoria.soporte.Fakes.RepositorioEnMemoria;
import com.rincon.auditoria.soporte.Fakes.UnidadDeTrabajoDirecta;
import com.rincon.auditoria.usecase.port.in.CerrarHallazgoUseCase;
import com.rincon.auditoria.usecase.port.in.EnviarAVerificacionUseCase;
import com.rincon.auditoria.usecase.port.in.IniciarRemediacionUseCase;
import com.rincon.auditoria.usecase.port.in.RechazarVerificacionUseCase;
import com.rincon.auditoria.usecase.port.in.RegistrarHallazgoUseCase;
import com.rincon.auditoria.usecase.service.CerrarHallazgoService;
import com.rincon.auditoria.usecase.service.ConsultarHallazgosService;
import com.rincon.auditoria.usecase.service.ConsultarHistorialService;
import com.rincon.auditoria.usecase.service.EnviarAVerificacionService;
import com.rincon.auditoria.usecase.service.IniciarRemediacionService;
import com.rincon.auditoria.usecase.service.RechazarVerificacionService;
import com.rincon.auditoria.usecase.service.RegistrarHallazgoService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CasosDeUsoTest {

    private static final Instant INICIO = Instant.parse("2026-03-02T09:00:00Z");

    private RepositorioEnMemoria repositorio;
    private HistorialEnMemoria historial;
    private RelojMutable reloj;
    private RegistrarHallazgoService registrar;
    private IniciarRemediacionService iniciar;
    private EnviarAVerificacionService verificar;
    private RechazarVerificacionService rechazar;
    private CerrarHallazgoService cerrar;
    private ConsultarHallazgosService consultar;
    private ConsultarHistorialService consultarHistorial;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioEnMemoria();
        historial = new HistorialEnMemoria();
        reloj = new RelojMutable(INICIO);
        UnidadDeTrabajoDirecta uow = new UnidadDeTrabajoDirecta();
        registrar = new RegistrarHallazgoService(repositorio, historial, uow, reloj);
        iniciar = new IniciarRemediacionService(repositorio, historial, uow, reloj);
        verificar = new EnviarAVerificacionService(repositorio, historial, uow, reloj);
        rechazar = new RechazarVerificacionService(repositorio, historial, uow, reloj);
        cerrar = new CerrarHallazgoService(repositorio, historial, uow, reloj);
        consultar = new ConsultarHallazgosService(repositorio);
        consultarHistorial = new ConsultarHistorialService(repositorio, historial);
    }

    private HallazgoAuditoria registrarEjemplo() {
        return registrar.ejecutar(new RegistrarHallazgoUseCase.Comando(
                "Backups sin cifrar", "Las copias de seguridad se almacenan sin cifrado", Severidad.ALTA));
    }

    private IniciarRemediacionUseCase.Comando comandoRemediacion(HallazgoId id) {
        return new IniciarRemediacionUseCase.Comando(id, "Luis Gómez", "Activar cifrado en el almacenamiento",
                LocalDate.of(2026, 3, 10));
    }

    @Test
    void registrar_persisteElHallazgoYAnotaElPrimerCambioEnLaBitacora() {
        HallazgoAuditoria h = registrarEjemplo();

        assertThat(repositorio.buscarPorId(h.getId())).isPresent();
        assertThat(historial.buscarPorHallazgo(h.getId())).singleElement().satisfies(c -> {
            assertThat(c.estadoAnterior()).isNull();
            assertThat(c.estadoNuevo()).isEqualTo(EstadoHallazgo.ABIERTO);
            assertThat(c.ocurridoEn()).isEqualTo(INICIO);
        });
    }

    @Test
    void flujoCompleto_dejaLaTrazaCronologicaYCierraElHallazgo() {
        HallazgoAuditoria h = registrarEjemplo();
        reloj.avanzar(Duration.ofHours(1));
        iniciar.ejecutar(comandoRemediacion(h.getId()));
        reloj.avanzar(Duration.ofHours(24));
        verificar.ejecutar(new EnviarAVerificacionUseCase.Comando(h.getId(), "Cifrado activado y comprobado"));
        reloj.avanzar(Duration.ofHours(2));
        HallazgoAuditoria cerrado = cerrar.ejecutar(new CerrarHallazgoUseCase.Comando(h.getId(), "Auditoría confirmó el cifrado"));

        assertThat(cerrado.getEstado()).isEqualTo(EstadoHallazgo.CERRADO);
        assertThat(cerrado.getHorasHastaCierre()).contains(27L);
        List<CambioEstado> traza = consultarHistorial.obtenerHistorial(h.getId());
        assertThat(traza).extracting(CambioEstado::estadoNuevo).containsExactly(
                EstadoHallazgo.ABIERTO, EstadoHallazgo.EN_REMEDIACION,
                EstadoHallazgo.EN_VERIFICACION, EstadoHallazgo.CERRADO);
        assertThat(traza).extracting(CambioEstado::ocurridoEn).isSorted();
    }

    @Test
    void rechazarVerificacion_agregaUnCambioDeRegresoALaBitacora() {
        HallazgoAuditoria h = registrarEjemplo();
        iniciar.ejecutar(comandoRemediacion(h.getId()));
        verificar.ejecutar(new EnviarAVerificacionUseCase.Comando(h.getId(), "Cifrado activado y comprobado"));

        rechazar.ejecutar(new RechazarVerificacionUseCase.Comando(h.getId(), "Falta cifrar el volumen secundario"));

        assertThat(consultar.obtenerPorId(h.getId()).getEstado()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
        assertThat(historial.buscarPorHallazgo(h.getId())).last().satisfies(c -> {
            assertThat(c.estadoAnterior()).isEqualTo(EstadoHallazgo.EN_VERIFICACION);
            assertThat(c.detalle()).contains("Falta cifrar el volumen secundario");
        });
    }

    @Test
    void transicionInvalida_noEscribeEnLaBitacoraNiCambiaElHallazgo() {
        HallazgoAuditoria h = registrarEjemplo();

        assertThatThrownBy(() -> cerrar.ejecutar(new CerrarHallazgoUseCase.Comando(h.getId(), "Cierre prematuro")))
                .isInstanceOf(TransicionInvalidaException.class);

        assertThat(historial.total()).isEqualTo(1);
        assertThat(consultar.obtenerPorId(h.getId()).getEstado()).isEqualTo(EstadoHallazgo.ABIERTO);
    }

    @Test
    void operarSobreUnHallazgoInexistente_lanzaNoEncontrado() {
        HallazgoId inexistente = HallazgoId.nuevo();

        assertThatThrownBy(() -> iniciar.ejecutar(comandoRemediacion(inexistente)))
                .isInstanceOf(HallazgoNoEncontradoException.class);
        assertThatThrownBy(() -> consultar.obtenerPorId(inexistente)).isInstanceOf(HallazgoNoEncontradoException.class);
        assertThatThrownBy(() -> consultarHistorial.obtenerHistorial(inexistente))
                .isInstanceOf(HallazgoNoEncontradoException.class);
    }

    @Test
    void listarPorEstado_filtraSoloLosQueCoinciden() {
        HallazgoAuditoria a = registrarEjemplo();
        registrarEjemplo();
        iniciar.ejecutar(comandoRemediacion(a.getId()));

        assertThat(consultar.listarTodos()).hasSize(2);
        assertThat(consultar.listarPorEstado(EstadoHallazgo.EN_REMEDIACION)).extracting(HallazgoAuditoria::getId)
                .containsExactly(a.getId());
        assertThat(consultar.listarPorEstado(EstadoHallazgo.CERRADO)).isEmpty();
    }
}
