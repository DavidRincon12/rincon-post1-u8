package com.rincon.auditoria.domain.model;

import com.rincon.auditoria.domain.exception.DatosInvalidosException;

/** Validación compartida de textos obligatorios para no repetirla en cada objeto del dominio. */
final class Texto {

    private Texto() {
    }

    static String requerir(String valor, String campo, int minimo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException("El campo '" + campo + "' es obligatorio");
        }
        String limpio = valor.strip();
        if (limpio.length() < minimo || limpio.length() > maximo) {
            throw new DatosInvalidosException(String.format(
                    "El campo '%s' debe tener entre %d y %d caracteres", campo, minimo, maximo));
        }
        return limpio;
    }
}
