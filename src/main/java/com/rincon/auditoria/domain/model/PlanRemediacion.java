package com.rincon.auditoria.domain.model;

import com.rincon.auditoria.domain.exception.DatosInvalidosException;
import java.time.LocalDate;

/**
 * Value Object inmutable: quién corrige, qué hará y para cuándo se compromete.
 * La coherencia con el SLA de la severidad la valida el agregado, que conoce ambos datos.
 */
public record PlanRemediacion(String responsable, String accion, LocalDate fechaCompromiso) {

    public PlanRemediacion {
        responsable = Texto.requerir(responsable, "responsable", 3, 100);
        accion = Texto.requerir(accion, "accion", 10, 1000);
        if (fechaCompromiso == null) {
            throw new DatosInvalidosException("El campo 'fechaCompromiso' es obligatorio");
        }
    }
}
