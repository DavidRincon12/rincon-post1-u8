package com.rincon.auditoria.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CerrarHallazgoRequest(@NotBlank String comentarioCierre) {
}
