package com.rincon.auditoria.infrastructure.web.dto;

import com.rincon.auditoria.domain.model.CambioEstado;
import com.rincon.auditoria.domain.model.EstadoHallazgo;
import java.time.Instant;

public record CambioEstadoResponse(EstadoHallazgo estadoAnterior, EstadoHallazgo estadoNuevo,
                                   Instant ocurridoEn, String detalle) {

    public static CambioEstadoResponse desde(CambioEstado c) {
        return new CambioEstadoResponse(c.estadoAnterior(), c.estadoNuevo(), c.ocurridoEn(), c.detalle());
    }
}
