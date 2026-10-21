package com.rincon.auditoria.domain.model;

import com.rincon.auditoria.domain.exception.DatosInvalidosException;
import java.util.UUID;

/** Value Object de identidad: evita la obsesión por primitivos y valida el formato en un solo lugar. */
public record HallazgoId(UUID valor) {

    public HallazgoId {
        if (valor == null) {
            throw new DatosInvalidosException("El identificador del hallazgo es obligatorio");
        }
    }

    public static HallazgoId nuevo() {
        return new HallazgoId(UUID.randomUUID());
    }

    public static HallazgoId de(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new DatosInvalidosException("El identificador del hallazgo es obligatorio");
        }
        try {
            return new HallazgoId(UUID.fromString(texto.strip()));
        } catch (IllegalArgumentException e) {
            throw new DatosInvalidosException("El identificador '" + texto + "' no es un UUID válido", e);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
