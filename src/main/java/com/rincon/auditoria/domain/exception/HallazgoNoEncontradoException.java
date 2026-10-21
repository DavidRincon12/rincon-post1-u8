package com.rincon.auditoria.domain.exception;

import com.rincon.auditoria.domain.model.HallazgoId;

/** Se lanza cuando se opera sobre un hallazgo que no existe en el repositorio. */
public class HallazgoNoEncontradoException extends DomainException {

    private static final long serialVersionUID = 1L;

    private final transient HallazgoId id;

    public HallazgoNoEncontradoException(HallazgoId id) {
        super("No existe un hallazgo con identificador " + id);
        this.id = id;
    }

    public HallazgoId getId() {
        return id;
    }
}
