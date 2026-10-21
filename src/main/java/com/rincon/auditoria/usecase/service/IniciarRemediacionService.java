package com.rincon.auditoria.usecase.service;

import com.rincon.auditoria.domain.exception.HallazgoNoEncontradoException;
import com.rincon.auditoria.domain.model.HallazgoAuditoria;
import com.rincon.auditoria.domain.model.PlanRemediacion;
import com.rincon.auditoria.usecase.port.in.IniciarRemediacionUseCase;
import com.rincon.auditoria.usecase.port.out.HallazgoRepositoryPort;
import com.rincon.auditoria.usecase.port.out.HistorialAuditoriaPort;
import com.rincon.auditoria.usecase.port.out.UnidadDeTrabajoPort;
import java.time.Clock;
import java.time.Instant;

public class IniciarRemediacionService implements IniciarRemediacionUseCase {

    private final HallazgoRepositoryPort repositorio;
    private final HistorialAuditoriaPort historial;
    private final UnidadDeTrabajoPort unidadDeTrabajo;
    private final Clock reloj;

    public IniciarRemediacionService(HallazgoRepositoryPort repositorio, HistorialAuditoriaPort historial,
            UnidadDeTrabajoPort unidadDeTrabajo, Clock reloj) {
        this.repositorio = repositorio;
        this.historial = historial;
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.reloj = reloj;
    }

    @Override
    public HallazgoAuditoria ejecutar(Comando comando) {
        // Cargar, mutar y guardar en una sola transacción para que el control de concurrencia sea efectivo.
        return unidadDeTrabajo.ejecutar(() -> {
            HallazgoAuditoria hallazgo = repositorio.buscarPorId(comando.id())
                    .orElseThrow(() -> new HallazgoNoEncontradoException(comando.id()));
            PlanRemediacion plan = new PlanRemediacion(comando.responsable(), comando.accion(), comando.fechaCompromiso());
            hallazgo.iniciarRemediacion(plan, Instant.now(reloj));
            historial.registrar(hallazgo.extraerCambiosPendientes());
            return repositorio.guardar(hallazgo);
        });
    }
}
