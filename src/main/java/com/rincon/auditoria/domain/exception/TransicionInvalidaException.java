package com.rincon.auditoria.domain.exception;

import com.rincon.auditoria.domain.model.EstadoHallazgo;

/** Se lanza cuando se intenta una transición que la máquina de estados no permite. */
public class TransicionInvalidaException extends DomainException {

    private static final long serialVersionUID = 1L;

    private final EstadoHallazgo origen;
    private final EstadoHallazgo destino;

    public TransicionInvalidaException(EstadoHallazgo origen, EstadoHallazgo destino) {
        super(String.format("Transición inválida: un hallazgo en estado %s no puede pasar a %s. Transiciones permitidas desde %s: %s",
                origen, destino, origen, origen.transicionesPermitidas()));
        this.origen = origen;
        this.destino = destino;
    }

    public EstadoHallazgo getOrigen() {
        return origen;
    }

    public EstadoHallazgo getDestino() {
        return destino;
    }
}
