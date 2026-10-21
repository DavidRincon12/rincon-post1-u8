package com.rincon.auditoria.infrastructure.web.dto;

import com.rincon.auditoria.domain.model.EstadoHallazgo;
import com.rincon.auditoria.domain.model.HallazgoAuditoria;
import com.rincon.auditoria.domain.model.Severidad;
import java.time.Instant;
import java.time.LocalDate;

/** Contrato de salida de la API: el agregado de dominio nunca se serializa directamente. */
public record HallazgoResponse(
        String id,
        String titulo,
        String descripcion,
        Severidad severidad,
        EstadoHallazgo estado,
        PlanRemediacionResponse planRemediacion,
        String evidenciaRemediacion,
        String comentarioCierre,
        Instant fechaRegistro,
        Instant fechaActualizacion,
        Instant fechaCierre,
        Long horasHastaCierre) {

    public record PlanRemediacionResponse(String responsable, String accion, LocalDate fechaCompromiso) {
    }

    public static HallazgoResponse desde(HallazgoAuditoria h) {
        return new HallazgoResponse(
                h.getId().toString(), h.getTitulo(), h.getDescripcion(), h.getSeveridad(), h.getEstado(),
                h.getPlanRemediacion()
                        .map(p -> new PlanRemediacionResponse(p.responsable(), p.accion(), p.fechaCompromiso()))
                        .orElse(null),
                h.getEvidenciaRemediacion().orElse(null),
                h.getComentarioCierre().orElse(null),
                h.getFechaRegistro(), h.getFechaActualizacion(),
                h.getFechaCierre().orElse(null), h.getHorasHastaCierre().orElse(null));
    }
}
