package com.rincon.auditoria.usecase.port.in;

import com.rincon.auditoria.domain.model.CambioEstado;
import com.rincon.auditoria.domain.model.HallazgoId;
import java.util.List;

public interface ConsultarHistorialUseCase {

    List<CambioEstado> obtenerHistorial(HallazgoId hallazgoId);
}
