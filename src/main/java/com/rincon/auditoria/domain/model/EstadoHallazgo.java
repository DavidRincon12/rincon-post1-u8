package com.rincon.auditoria.domain.model;

import com.rincon.auditoria.domain.exception.TransicionInvalidaException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Máquina de estados del ciclo de vida de un hallazgo.
 *
 * <pre>
 * ABIERTO → EN_REMEDIACION → EN_VERIFICACION → CERRADO
 *                ↑                  │
 *                └──── (rechazo) ───┘
 * </pre>
 *
 * CERRADO es terminal: un hallazgo cerrado no se reabre, se registra uno nuevo.
 */
public enum EstadoHallazgo {
    ABIERTO,
    EN_REMEDIACION,
    EN_VERIFICACION,
    CERRADO;

    public Set<EstadoHallazgo> transicionesPermitidas() {
        EnumSet<EstadoHallazgo> permitidas = switch (this) {
            case ABIERTO -> EnumSet.of(EN_REMEDIACION);
            case EN_REMEDIACION -> EnumSet.of(EN_VERIFICACION);
            case EN_VERIFICACION -> EnumSet.of(EN_REMEDIACION, CERRADO);
            case CERRADO -> EnumSet.noneOf(EstadoHallazgo.class);
        };
        return Collections.unmodifiableSet(permitidas);
    }

    public boolean puedeTransicionarA(EstadoHallazgo destino) {
        return destino != null && transicionesPermitidas().contains(destino);
    }

    /** @throws TransicionInvalidaException si la transición no está en la tabla de la máquina de estados */
    public void validarTransicionA(EstadoHallazgo destino) {
        if (!puedeTransicionarA(destino)) {
            throw new TransicionInvalidaException(this, destino);
        }
    }

    public boolean esTerminal() {
        return transicionesPermitidas().isEmpty();
    }
}
