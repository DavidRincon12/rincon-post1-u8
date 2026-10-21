package com.rincon.auditoria.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record IniciarRemediacionRequest(
        @NotBlank String responsable,
        @NotBlank String accion,
        @NotNull LocalDate fechaCompromiso) {
}
