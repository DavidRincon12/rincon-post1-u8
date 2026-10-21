package com.rincon.auditoria.usecase.port.out;

import com.rincon.auditoria.domain.model.CambioEstado;
import com.rincon.auditoria.domain.model.HallazgoId;
import java.util.List;

/**
 * Puerto de la bitácora de trazabilidad. Solo permite agregar y consultar:
 * no existe operación para modificar ni borrar, por lo que el historial es append-only por contrato.
 */
public interface HistorialAuditoriaPort {

    void registrar(List<CambioEstado> cambios);

    /** Devuelve los cambios del hallazgo en orden cronológico ascendente. */
    List<CambioEstado> buscarPorHallazgo(HallazgoId hallazgoId);
}
