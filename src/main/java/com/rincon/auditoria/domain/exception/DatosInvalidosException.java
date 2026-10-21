package com.rincon.auditoria.domain.exception;

/** Se lanza cuando un dato viola una invariante del dominio (nulo, vacío, fuera de rango). */
public class DatosInvalidosException extends DomainException {

    private static final long serialVersionUID = 1L;

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }

    public DatosInvalidosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
