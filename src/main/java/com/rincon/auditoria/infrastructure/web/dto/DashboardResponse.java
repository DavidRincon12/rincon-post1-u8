package com.rincon.auditoria.infrastructure.web.dto;

import com.rincon.auditoria.domain.model.EstadoHallazgo;
import com.rincon.auditoria.domain.model.Severidad;
import com.rincon.auditoria.usecase.dto.ConteoPorEstado;
import com.rincon.auditoria.usecase.dto.ConteoPorSeveridad;
import com.rincon.auditoria.usecase.dto.DashboardAuditoria;
import com.rincon.auditoria.usecase.dto.TiempoPromedioCierre;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalHallazgos,
        Map<EstadoHallazgo, Long> porEstado,
        Map<Severidad, Long> porSeveridad,
        List<TiempoCierreResponse> tiempoPromedioCierre,
        double promedioGeneralDiasCierre) {

    public record TiempoCierreResponse(Severidad severidad, double promedioHoras, double promedioDias,
                                       long hallazgosCerrados) {
    }

    public static DashboardResponse desde(DashboardAuditoria d) {
        Map<EstadoHallazgo, Long> porEstado = new LinkedHashMap<>();
        for (ConteoPorEstado c : d.porEstado()) {
            porEstado.put(c.estado(), c.total());
        }
        Map<Severidad, Long> porSeveridad = new LinkedHashMap<>();
        for (ConteoPorSeveridad c : d.porSeveridad()) {
            porSeveridad.put(c.severidad(), c.total());
        }
        List<TiempoCierreResponse> tiempos = d.tiempoPromedioCierrePorSeveridad().stream()
                .map(DashboardResponse::aTiempo).toList();
        return new DashboardResponse(d.totalHallazgos(), porEstado, porSeveridad, tiempos, d.promedioGeneralDiasCierre());
    }

    private static TiempoCierreResponse aTiempo(TiempoPromedioCierre t) {
        double dias = Math.round(t.promedioHoras() / 24.0 * 100.0) / 100.0;
        return new TiempoCierreResponse(t.severidad(), t.promedioHoras(), dias, t.hallazgosCerrados());
    }
}
