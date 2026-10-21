package com.rincon.auditoria.usecase.service;

import com.rincon.auditoria.domain.model.HallazgoAuditoria;
import com.rincon.auditoria.usecase.port.in.RegistrarHallazgoUseCase;
import com.rincon.auditoria.usecase.port.out.HallazgoRepositoryPort;
import com.rincon.auditoria.usecase.port.out.HistorialAuditoriaPort;
import com.rincon.auditoria.usecase.port.out.UnidadDeTrabajoPort;
import java.time.Clock;
import java.time.Instant;

public class RegistrarHallazgoService implements RegistrarHallazgoUseCase {

    private final HallazgoRepositoryPort repositorio;
    private final HistorialAuditoriaPort historial;
    private final UnidadDeTrabajoPort unidadDeTrabajo;
    private final Clock reloj;

    public RegistrarHallazgoService(HallazgoRepositoryPort repositorio, HistorialAuditoriaPort historial,
            UnidadDeTrabajoPort unidadDeTrabajo, Clock reloj) {
        this.repositorio = repositorio;
        this.historial = historial;
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.reloj = reloj;
    }

    @Override
    public HallazgoAuditoria ejecutar(Comando comando) {
        HallazgoAuditoria hallazgo = HallazgoAuditoria.registrar(
                comando.titulo(), comando.descripcion(), comando.severidad(), Instant.now(reloj));
        return unidadDeTrabajo.ejecutar(() -> {
            historial.registrar(hallazgo.extraerCambiosPendientes());
            return repositorio.guardar(hallazgo);
        });
    }
}
