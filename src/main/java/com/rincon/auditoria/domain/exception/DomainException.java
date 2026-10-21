package com.rincon.auditoria.domain.exception;

/**
 * Raíz de las excepciones de negocio. Al ser no verificada, los casos de uso no
 * contaminan sus firmas y un único manejador en el borde las traduce a HTTP.
 */
public abstract class DomainException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected DomainException(String mensaje) {
        super(mensaje);
    }

    protected DomainException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
