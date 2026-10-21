package com.rincon.auditoria.usecase.dto;

import com.rincon.auditoria.domain.model.Severidad;

/** Tiempo medio, en horas, que tardaron en cerrarse los hallazgos de una severidad. */
public record TiempoPromedioCierre(Severidad severidad, double promedioHoras, long hallazgosCerrados) {
}
