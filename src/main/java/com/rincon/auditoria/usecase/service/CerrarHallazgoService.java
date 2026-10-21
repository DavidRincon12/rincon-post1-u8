package com.rincon.auditoria.usecase.service;

import com.rincon.auditoria.domain.exception.HallazgoNoEncontradoException;
import com.rincon.auditoria.domain.model.HallazgoAuditoria;
import com.rincon.auditoria.usecase.port.in.CerrarHallazgoUseCase;
import com.rincon.auditoria.usecase.port.out.HallazgoRepositoryPort;
import com.rincon.auditoria.usecase.port.out.UnidadDeTrabajoPort;
import java.time.Clock;
import java.time.Instant;

public class CerrarHallazgoService implements CerrarHallazgoUseCase {

    private final HallazgoRepositoryPort repositorio;
    private final UnidadDeTrabajoPort unidadDeTrabajo;
    private final Clock reloj;

    public CerrarHallazgoService(HallazgoRepositoryPort repositorio, UnidadDeTrabajoPort unidadDeTrabajo, Clock reloj) {
        this.repositorio = repositorio;
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.reloj = reloj;
    }

    @Override
    public HallazgoAuditoria ejecutar(Comando comando) {
        // Cargar, mutar y guardar en una sola transacción para que el control de concurrencia sea efectivo.
        return unidadDeTrabajo.ejecutar(() -> {
            HallazgoAuditoria hallazgo = repositorio.buscarPorId(comando.id())
                    .orElseThrow(() -> new HallazgoNoEncontradoException(comando.id()));
            hallazgo.cerrar(comando.comentarioCierre(), Instant.now(reloj));
            return repositorio.guardar(hallazgo);
        });
    }
}
