package com.rincon.auditoria.usecase.port.in;

import com.rincon.auditoria.domain.model.HallazgoAuditoria;
import com.rincon.auditoria.domain.model.Severidad;

public interface RegistrarHallazgoUseCase {

    HallazgoAuditoria ejecutar(Comando comando);

    record Comando(String titulo, String descripcion, Severidad severidad) {
    }
}
