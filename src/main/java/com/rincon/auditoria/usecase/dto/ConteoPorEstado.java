package com.rincon.auditoria.usecase.dto;

import com.rincon.auditoria.domain.model.EstadoHallazgo;

public record ConteoPorEstado(EstadoHallazgo estado, long total) {
}
