package com.rincon.auditoria.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.rincon.auditoria.domain.model.EstadoHallazgo;
import com.rincon.auditoria.domain.model.HallazgoAuditoria;
import com.rincon.auditoria.domain.model.HallazgoId;
import com.rincon.auditoria.domain.model.Severidad;
import com.rincon.auditoria.soporte.Fakes.RepositorioEnMemoria;
import com.rincon.auditoria.usecase.dto.ConteoPorEstado;
import com.rincon.auditoria.usecase.dto.ConteoPorSeveridad;
import com.rincon.auditoria.usecase.dto.DashboardAuditoria;
import com.rincon.auditoria.usecase.dto.TiempoPromedioCierre;
import com.rincon.auditoria.usecase.service.ObtenerDashboardAuditoriaService;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ObtenerDashboardAuditoriaServiceTest {

    private static final Instant REGISTRO = Instant.parse("2026-03-02T09:00:00Z");

    private final RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
    private final ObtenerDashboardAuditoriaService servicio = new ObtenerDashboardAuditoriaService(repositorio);

    private void abierto(Severidad severidad) {
        repositorio.guardar(HallazgoAuditoria.registrar("Hallazgo de ejemplo", "Descripción de ejemplo", severidad, REGISTRO));
    }

    private void cerrado(Severidad severidad, long horas) {
        repositorio.guardar(HallazgoAuditoria.reconstruir(HallazgoId.nuevo(), "Hallazgo cerrado", "Descripción de ejemplo",
                severidad, EstadoHallazgo.CERRADO, null, null, "Cerrado", REGISTRO, REGISTRO.plusSeconds(horas * 3600),
                REGISTRO.plusSeconds(horas * 3600), horas));
    }

    @Test
    void sinDatos_devuelveTodosLosValoresEnCero() {
        DashboardAuditoria d = servicio.ejecutar();

        assertThat(d.totalHallazgos()).isZero();
        assertThat(d.porEstado()).hasSize(EstadoHallazgo.values().length).allMatch(c -> c.total() == 0);
        assertThat(d.porSeveridad()).hasSize(Severidad.values().length).allMatch(c -> c.total() == 0);
        assertThat(d.promedioGeneralDiasCierre()).isZero();
    }

    @Test
    void conDatos_agrupaPorEstadoYSeveridadCompletandoLosVacios() {
        abierto(Severidad.CRITICA);
        abierto(Severidad.CRITICA);
        abierto(Severidad.BAJA);
        cerrado(Severidad.ALTA, 48);

        DashboardAuditoria d = servicio.ejecutar();

        assertThat(d.totalHallazgos()).isEqualTo(4);
        assertThat(d.porEstado()).contains(new ConteoPorEstado(EstadoHallazgo.ABIERTO, 3),
                new ConteoPorEstado(EstadoHallazgo.CERRADO, 1), new ConteoPorEstado(EstadoHallazgo.EN_REMEDIACION, 0));
        assertThat(d.porSeveridad()).contains(new ConteoPorSeveridad(Severidad.CRITICA, 2),
                new ConteoPorSeveridad(Severidad.MEDIA, 0));
    }

    @Test
    void promedioDeCierre_seCalculaPorSeveridadYElGeneralEsPonderado() {
        cerrado(Severidad.ALTA, 24);
        cerrado(Severidad.ALTA, 72);
        cerrado(Severidad.BAJA, 96);
        abierto(Severidad.ALTA);

        DashboardAuditoria d = servicio.ejecutar();

        assertThat(d.tiempoPromedioCierrePorSeveridad()).contains(
                new TiempoPromedioCierre(Severidad.ALTA, 48.0, 2),
                new TiempoPromedioCierre(Severidad.BAJA, 96.0, 1),
                new TiempoPromedioCierre(Severidad.CRITICA, 0.0, 0));
        // (24 + 72 + 96) / 3 = 64 horas = 2,67 días
        assertThat(d.promedioGeneralDiasCierre()).isEqualTo(2.67);
    }
}
