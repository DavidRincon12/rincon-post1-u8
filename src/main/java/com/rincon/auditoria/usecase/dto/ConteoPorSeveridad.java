package com.rincon.auditoria.usecase.dto;

import com.rincon.auditoria.domain.model.Severidad;

public record ConteoPorSeveridad(Severidad severidad, long total) {
}
