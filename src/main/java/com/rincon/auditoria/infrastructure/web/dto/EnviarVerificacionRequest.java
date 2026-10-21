package com.rincon.auditoria.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record EnviarVerificacionRequest(@NotBlank String evidencia) {
}
