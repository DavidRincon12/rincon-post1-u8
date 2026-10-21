package com.rincon.auditoria.domain.model;

import com.rincon.auditoria.domain.exception.DatosInvalidosException;
import java.time.Instant;

/**
 * Hecho inmutable: el hallazgo pasó de un estado a otro en un momento dado.
 * El estado anterior es nulo únicamente en el registro inicial.
 */
public record CambioEstado(HallazgoId hallazgoId, EstadoHallazgo estadoAnterior, EstadoHallazgo estadoNuevo,
                           Instant ocurridoEn, String detalle) {

    public CambioEstado {
        if (hallazgoId == null || estadoNuevo == null || ocurridoEn == null) {
            throw new DatosInvalidosException("El cambio de estado requiere hallazgo, estado nuevo y fecha");
        }
    }
}
