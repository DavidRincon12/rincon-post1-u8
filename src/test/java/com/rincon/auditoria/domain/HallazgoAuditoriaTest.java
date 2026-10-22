package com.rincon.auditoria.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rincon.auditoria.domain.exception.DatosInvalidosException;
import com.rincon.auditoria.domain.exception.PlanRemediacionInvalidoException;
import com.rincon.auditoria.domain.exception.TransicionInvalidaException;
import com.rincon.auditoria.domain.model.CambioEstado;
import com.rincon.auditoria.domain.model.EstadoHallazgo;
import com.rincon.auditoria.domain.model.HallazgoAuditoria;
import com.rincon.auditoria.domain.model.PlanRemediacion;
import com.rincon.auditoria.domain.model.Severidad;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class HallazgoAuditoriaTest {

    private static final Instant REGISTRO = Instant.parse("2026-03-02T09:00:00Z");
    private static final LocalDate HOY = LocalDate.of(2026, 3, 2);

    private static HallazgoAuditoria nuevo(Severidad severidad) {
        return HallazgoAuditoria.registrar("Contraseñas sin cifrar", "El módulo de login guarda claves en texto plano",
                severidad, REGISTRO);
    }

    private static PlanRemediacion plan(LocalDate fecha) {
        return new PlanRemediacion("Ana Pérez", "Migrar el almacenamiento a Argon2id", fecha);
    }

    private static HallazgoAuditoria enVerificacion() {
        HallazgoAuditoria h = nuevo(Severidad.ALTA);
        h.iniciarRemediacion(plan(HOY.plusDays(10)), REGISTRO.plusSeconds(60));
        h.enviarAVerificacion("Pull request 42 integrado y desplegado", REGISTRO.plusSeconds(120));
        return h;
    }

    @Test
    void registrar_creaHallazgoAbiertoSinPlan() {
        HallazgoAuditoria h = nuevo(Severidad.MEDIA);

        assertThat(h.getId()).isNotNull();
        assertThat(h.getEstado()).isEqualTo(EstadoHallazgo.ABIERTO);
        assertThat(h.getPlanRemediacion()).isEmpty();
        assertThat(h.getFechaCierre()).isEmpty();
        assertThat(h.getFechaRegistro()).isEqualTo(REGISTRO);
        assertThat(h.getFechaActualizacion()).isEqualTo(REGISTRO);
    }

    @Test
    void registrar_conDatosInvalidos_lanzaDatosInvalidos() {
        assertThatThrownBy(() -> HallazgoAuditoria.registrar(" ", "Descripción suficientemente larga", Severidad.BAJA, REGISTRO))
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("titulo");
        assertThatThrownBy(() -> HallazgoAuditoria.registrar("Título válido", "corta", Severidad.BAJA, REGISTRO))
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("descripcion");
        assertThatThrownBy(() -> HallazgoAuditoria.registrar("Título válido", "Descripción suficientemente larga", null, REGISTRO))
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("severidad");
        assertThatThrownBy(() -> HallazgoAuditoria.registrar("x".repeat(151), "Descripción suficientemente larga", Severidad.BAJA, REGISTRO))
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("titulo");
        assertThatThrownBy(() -> HallazgoAuditoria.registrar("Título válido", "Descripción suficientemente larga", Severidad.BAJA, null))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void iniciarRemediacion_desdeAbierto_guardaElPlanYCambiaElEstado() {
        HallazgoAuditoria h = nuevo(Severidad.ALTA);
        Instant ahora = REGISTRO.plusSeconds(3600);

        h.iniciarRemediacion(plan(HOY.plusDays(10)), ahora);

        assertThat(h.getEstado()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
        assertThat(h.getPlanRemediacion()).contains(plan(HOY.plusDays(10)));
        assertThat(h.getFechaActualizacion()).isEqualTo(ahora);
    }

    @Test
    void iniciarRemediacion_fechaEnElPasado_lanzaPlanInvalidoYNoCambiaNada() {
        HallazgoAuditoria h = nuevo(Severidad.ALTA);

        assertThatThrownBy(() -> h.iniciarRemediacion(plan(HOY.minusDays(1)), REGISTRO))
                .isInstanceOf(PlanRemediacionInvalidoException.class).hasMessageContaining("pasado");

        assertThat(h.getEstado()).isEqualTo(EstadoHallazgo.ABIERTO);
        assertThat(h.getPlanRemediacion()).isEmpty();
    }

    @Test
    void iniciarRemediacion_fueraDelSlaDeLaSeveridad_lanzaPlanInvalido() {
        HallazgoAuditoria critico = nuevo(Severidad.CRITICA);

        assertThatThrownBy(() -> critico.iniciarRemediacion(plan(HOY.plusDays(8)), REGISTRO))
                .isInstanceOf(PlanRemediacionInvalidoException.class).hasMessageContaining("CRITICA");
    }

    @Test
    void iniciarRemediacion_enElLimiteExactoDelSla_esValido() {
        HallazgoAuditoria critico = nuevo(Severidad.CRITICA);

        critico.iniciarRemediacion(plan(HOY.plusDays(7)), REGISTRO);

        assertThat(critico.getEstado()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
    }

    @Test
    void iniciarRemediacion_sinPlan_lanzaDatosInvalidos() {
        assertThatThrownBy(() -> nuevo(Severidad.BAJA).iniciarRemediacion(null, REGISTRO))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void iniciarRemediacion_dosVeces_lanzaTransicionInvalida() {
        HallazgoAuditoria h = nuevo(Severidad.ALTA);
        h.iniciarRemediacion(plan(HOY.plusDays(10)), REGISTRO);

        assertThatThrownBy(() -> h.iniciarRemediacion(plan(HOY.plusDays(10)), REGISTRO))
                .isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    void iniciarRemediacion_desdeEnVerificacion_lanzaTransicionInvalida() {
        HallazgoAuditoria h = enVerificacion();

        assertThatThrownBy(() -> h.iniciarRemediacion(plan(HOY.plusDays(10)), REGISTRO.plusSeconds(300)))
                .isInstanceOfSatisfying(TransicionInvalidaException.class, e -> {
                    assertThat(e.getOrigen()).isEqualTo(EstadoHallazgo.EN_VERIFICACION);
                    assertThat(e.getDestino()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
                });
        assertThat(h.getEstado()).isEqualTo(EstadoHallazgo.EN_VERIFICACION);
    }

    @Test
    void cerrar_desdeAbierto_lanzaTransicionInvalidaYConservaElEstado() {
        HallazgoAuditoria h = nuevo(Severidad.ALTA);
        h.extraerCambiosPendientes();

        assertThatThrownBy(() -> h.cerrar("Se cierra sin remediar", REGISTRO.plusSeconds(10)))
                .isInstanceOfSatisfying(TransicionInvalidaException.class, e -> {
                    assertThat(e.getOrigen()).isEqualTo(EstadoHallazgo.ABIERTO);
                    assertThat(e.getDestino()).isEqualTo(EstadoHallazgo.CERRADO);
                });

        assertThat(h.getEstado()).isEqualTo(EstadoHallazgo.ABIERTO);
        assertThat(h.getFechaCierre()).isEmpty();
        assertThat(h.getFechaActualizacion()).isEqualTo(REGISTRO);
        assertThat(h.extraerCambiosPendientes()).isEmpty();
    }

    @Test
    void enviarAVerificacion_desdeAbierto_lanzaTransicionInvalida() {
        assertThatThrownBy(() -> nuevo(Severidad.ALTA).enviarAVerificacion("Evidencia suficiente", REGISTRO))
                .isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    void enviarAVerificacion_sinEvidencia_lanzaDatosInvalidosYNoCambiaElEstado() {
        HallazgoAuditoria h = nuevo(Severidad.ALTA);
        h.iniciarRemediacion(plan(HOY.plusDays(10)), REGISTRO);

        assertThatThrownBy(() -> h.enviarAVerificacion(" ", REGISTRO))
                .isInstanceOf(DatosInvalidosException.class);

        assertThat(h.getEstado()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
    }

    @Test
    void cerrar_sinComentario_lanzaDatosInvalidosYNoCierra() {
        HallazgoAuditoria h = enVerificacion();

        assertThatThrownBy(() -> h.cerrar(null, REGISTRO.plusSeconds(200)))
                .isInstanceOf(DatosInvalidosException.class);

        assertThat(h.getEstado()).isEqualTo(EstadoHallazgo.EN_VERIFICACION);
        assertThat(h.getFechaCierre()).isEmpty();
    }

    @Test
    void flujoCompleto_llegaACerradoYCalculaElTiempoHastaElCierre() {
        HallazgoAuditoria h = enVerificacion();
        Instant cierre = REGISTRO.plusSeconds(50 * 3600 + 1800);

        h.cerrar("Verificado en el ambiente de pruebas", cierre);

        assertThat(h.getEstado()).isEqualTo(EstadoHallazgo.CERRADO);
        assertThat(h.getComentarioCierre()).contains("Verificado en el ambiente de pruebas");
        assertThat(h.getFechaCierre()).contains(cierre);
        assertThat(h.getHorasHastaCierre()).contains(50L);
        assertThat(h.getEvidenciaRemediacion()).isPresent();
    }

    @Test
    void rechazarVerificacion_vuelveARemediacionYDescartaLaEvidencia() {
        HallazgoAuditoria h = enVerificacion();

        h.rechazarVerificacion("La evidencia no demuestra el cifrado", REGISTRO.plusSeconds(300));

        assertThat(h.getEstado()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
        assertThat(h.getEvidenciaRemediacion()).isEmpty();
        assertThat(h.getPlanRemediacion()).isPresent();
    }

    @Test
    void rechazarVerificacion_desdeAbierto_lanzaTransicionInvalida() {
        assertThatThrownBy(() -> nuevo(Severidad.BAJA).rechazarVerificacion("Motivo suficiente", REGISTRO))
                .isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    void hallazgoCerrado_esTerminal_ningunaOperacionEsPosible() {
        HallazgoAuditoria h = enVerificacion();
        h.cerrar("Verificado en el ambiente de pruebas", REGISTRO.plusSeconds(600));

        assertThatThrownBy(() -> h.iniciarRemediacion(plan(HOY.plusDays(5)), REGISTRO))
                .isInstanceOf(TransicionInvalidaException.class);
        assertThatThrownBy(() -> h.enviarAVerificacion("Evidencia suficiente", REGISTRO))
                .isInstanceOf(TransicionInvalidaException.class);
        assertThatThrownBy(() -> h.rechazarVerificacion("Motivo suficiente", REGISTRO))
                .isInstanceOf(TransicionInvalidaException.class);
        assertThatThrownBy(() -> h.cerrar("Cierre repetido", REGISTRO))
                .isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    void extraerCambiosPendientes_devuelveLaTrazaEnOrdenYLuegoQuedaVacia() {
        HallazgoAuditoria h = enVerificacion();
        h.rechazarVerificacion("La evidencia no demuestra el cifrado", REGISTRO.plusSeconds(300));

        List<CambioEstado> cambios = h.extraerCambiosPendientes();

        assertThat(cambios).extracting(CambioEstado::estadoNuevo).containsExactly(
                EstadoHallazgo.ABIERTO, EstadoHallazgo.EN_REMEDIACION,
                EstadoHallazgo.EN_VERIFICACION, EstadoHallazgo.EN_REMEDIACION);
        assertThat(cambios).extracting(CambioEstado::estadoAnterior).containsExactly(
                null, EstadoHallazgo.ABIERTO, EstadoHallazgo.EN_REMEDIACION, EstadoHallazgo.EN_VERIFICACION);
        assertThat(cambios).allSatisfy(c -> assertThat(c.hallazgoId()).isEqualTo(h.getId()));
        assertThat(h.extraerCambiosPendientes()).isEmpty();
    }

    @Test
    void reconstruir_rehidrataSinGenerarCambiosPendientes() {
        HallazgoAuditoria original = enVerificacion();

        HallazgoAuditoria copia = HallazgoAuditoria.reconstruir(original.getId(), original.getTitulo(),
                original.getDescripcion(), original.getSeveridad(), original.getEstado(),
                original.getPlanRemediacion().orElse(null), original.getEvidenciaRemediacion().orElse(null),
                null, original.getFechaRegistro(), original.getFechaActualizacion(), null, null);

        assertThat(copia).isEqualTo(original);
        assertThat(copia.getEstado()).isEqualTo(EstadoHallazgo.EN_VERIFICACION);
        assertThat(copia.extraerCambiosPendientes()).isEmpty();
    }
}
