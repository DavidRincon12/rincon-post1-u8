package com.rincon.auditoria.usecase.service;

import com.rincon.auditoria.domain.exception.DatosInvalidosException;
import com.rincon.auditoria.domain.exception.HallazgoNoEncontradoException;
import com.rincon.auditoria.domain.model.EstadoHallazgo;
import com.rincon.auditoria.domain.model.HallazgoAuditoria;
import com.rincon.auditoria.domain.model.HallazgoId;
import com.rincon.auditoria.usecase.port.in.ConsultarHallazgosUseCase;
import com.rincon.auditoria.usecase.port.out.HallazgoRepositoryPort;
import java.util.List;

public class ConsultarHallazgosService implements ConsultarHallazgosUseCase {

    private final HallazgoRepositoryPort repositorio;

    public ConsultarHallazgosService(HallazgoRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public HallazgoAuditoria obtenerPorId(HallazgoId id) {
        if (id == null) {
            throw new DatosInvalidosException("El identificador del hallazgo es obligatorio");
        }
        return repositorio.buscarPorId(id).orElseThrow(() -> new HallazgoNoEncontradoException(id));
    }

    @Override
    public List<HallazgoAuditoria> listarTodos() {
        return repositorio.buscarTodos();
    }

    @Override
    public List<HallazgoAuditoria> listarPorEstado(EstadoHallazgo estado) {
        if (estado == null) {
            throw new DatosInvalidosException("El estado es obligatorio para filtrar");
        }
        return repositorio.buscarPorEstado(estado);
    }
}
