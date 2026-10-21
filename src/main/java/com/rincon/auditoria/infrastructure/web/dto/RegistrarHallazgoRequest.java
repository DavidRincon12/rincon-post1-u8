package com.rincon.auditoria.infrastructure.web.dto;

import com.rincon.auditoria.domain.model.Severidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrarHallazgoRequest(
        @NotBlank String titulo,
        @NotBlank String descripcion,
        @NotNull Severidad severidad) {
}
