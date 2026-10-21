package com.rincon.auditoria.usecase.dto;

import java.util.List;

/**
 * Modelo de lectura del tablero. Las listas traen siempre todos los valores del enum
 * (con total 0 cuando no hay datos) para que el consumidor no deba completar vacíos.
 */
public record DashboardAuditoria(
        long totalHallazgos,
        List<ConteoPorEstado> porEstado,
        List<ConteoPorSeveridad> porSeveridad,
        List<TiempoPromedioCierre> tiempoPromedioCierrePorSeveridad,
        double promedioGeneralDiasCierre) {
}
