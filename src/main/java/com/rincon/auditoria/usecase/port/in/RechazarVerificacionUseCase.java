package com.rincon.auditoria.usecase.port.in;

import com.rincon.auditoria.domain.exception.DatosInvalidosException;
import com.rincon.auditoria.domain.model.HallazgoAuditoria;
import com.rincon.auditoria.domain.model.HallazgoId;

public interface RechazarVerificacionUseCase {

    HallazgoAuditoria ejecutar(Comando comando);

    record Comando(HallazgoId id, String motivo) {

        public Comando {
            if (id == null) {
                throw new DatosInvalidosException("El identificador del hallazgo es obligatorio");
            }
        }
    }
}
