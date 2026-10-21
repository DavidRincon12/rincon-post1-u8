package com.rincon.auditoria.usecase.service;

import com.rincon.auditoria.domain.exception.DatosInvalidosException;
import com.rincon.auditoria.domain.exception.HallazgoNoEncontradoException;
import com.rincon.auditoria.domain.model.CambioEstado;
import com.rincon.auditoria.domain.model.HallazgoId;
import com.rincon.auditoria.usecase.port.in.ConsultarHistorialUseCase;
import com.rincon.auditoria.usecase.port.out.HallazgoRepositoryPort;
import com.rincon.auditoria.usecase.port.out.HistorialAuditoriaPort;
import java.util.List;

public class ConsultarHistorialService implements ConsultarHistorialUseCase {

    private final HallazgoRepositoryPort repositorio;
    private final HistorialAuditoriaPort historial;

    public ConsultarHistorialService(HallazgoRepositoryPort repositorio, HistorialAuditoriaPort historial) {
        this.repositorio = repositorio;
        this.historial = historial;
    }

    @Override
    public List<CambioEstado> obtenerHistorial(HallazgoId hallazgoId) {
        if (hallazgoId == null) {
            throw new DatosInvalidosException("El identificador del hallazgo es obligatorio");
        }
        // Distingue "hallazgo inexistente" (404) de "hallazgo sin cambios".
        repositorio.buscarPorId(hallazgoId).orElseThrow(() -> new HallazgoNoEncontradoException(hallazgoId));
        return historial.buscarPorHallazgo(hallazgoId);
    }
}
