package com.rincon.auditoria.usecase.service;

import com.rincon.auditoria.domain.model.EstadoHallazgo;
import com.rincon.auditoria.domain.model.Severidad;
import com.rincon.auditoria.usecase.dto.ConteoPorEstado;
import com.rincon.auditoria.usecase.dto.ConteoPorSeveridad;
import com.rincon.auditoria.usecase.dto.DashboardAuditoria;
import com.rincon.auditoria.usecase.dto.TiempoPromedioCierre;
import com.rincon.auditoria.usecase.port.in.ObtenerDashboardAuditoriaUseCase;
import com.rincon.auditoria.usecase.port.out.HallazgoRepositoryPort;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ObtenerDashboardAuditoriaService implements ObtenerDashboardAuditoriaUseCase {

    private static final double HORAS_POR_DIA = 24.0;

    private final HallazgoRepositoryPort repositorio;

    public ObtenerDashboardAuditoriaService(HallazgoRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public DashboardAuditoria ejecutar() {
        Map<EstadoHallazgo, Long> porEstado = repositorio.contarPorEstado().stream()
                .collect(Collectors.toMap(ConteoPorEstado::estado, ConteoPorEstado::total));
        Map<Severidad, Long> porSeveridad = repositorio.contarPorSeveridad().stream()
                .collect(Collectors.toMap(ConteoPorSeveridad::severidad, ConteoPorSeveridad::total));
        Map<Severidad, TiempoPromedioCierre> tiempos = repositorio.promedioHorasCierrePorSeveridad().stream()
                .collect(Collectors.toMap(TiempoPromedioCierre::severidad, Function.identity()));

        List<ConteoPorEstado> estadosCompletos = Arrays.stream(EstadoHallazgo.values())
                .map(e -> new ConteoPorEstado(e, porEstado.getOrDefault(e, 0L))).toList();
        List<ConteoPorSeveridad> severidadesCompletas = Arrays.stream(Severidad.values())
                .map(s -> new ConteoPorSeveridad(s, porSeveridad.getOrDefault(s, 0L))).toList();
        List<TiempoPromedioCierre> tiemposCompletos = Arrays.stream(Severidad.values())
                .map(s -> tiempos.getOrDefault(s, new TiempoPromedioCierre(s, 0.0, 0L))).toList();

        long total = estadosCompletos.stream().mapToLong(ConteoPorEstado::total).sum();
        return new DashboardAuditoria(total, estadosCompletos, severidadesCompletas,
                tiemposCompletos.stream().map(ObtenerDashboardAuditoriaService::redondear).toList(), promedioGeneralDias(tiemposCompletos));
    }

    /** Promedio ponderado por cantidad de cierres: equivale al AVG global sin otra consulta. */
    private static double promedioGeneralDias(List<TiempoPromedioCierre> tiempos) {
        long cerrados = tiempos.stream().mapToLong(TiempoPromedioCierre::hallazgosCerrados).sum();
        if (cerrados == 0) {
            return 0.0;
        }
        double horasTotales = tiempos.stream().mapToDouble(t -> t.promedioHoras() * t.hallazgosCerrados()).sum();
        return redondear(horasTotales / cerrados / HORAS_POR_DIA);
    }

    private static TiempoPromedioCierre redondear(TiempoPromedioCierre t) {
        return new TiempoPromedioCierre(t.severidad(), redondear(t.promedioHoras()), t.hallazgosCerrados());
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
