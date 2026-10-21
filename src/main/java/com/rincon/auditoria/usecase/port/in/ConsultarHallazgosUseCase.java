package com.rincon.auditoria.usecase.port.in;

import com.rincon.auditoria.domain.model.EstadoHallazgo;
import com.rincon.auditoria.domain.model.HallazgoAuditoria;
import com.rincon.auditoria.domain.model.HallazgoId;
import java.util.List;

public interface ConsultarHallazgosUseCase {

    HallazgoAuditoria obtenerPorId(HallazgoId id);

    List<HallazgoAuditoria> listarTodos();

    List<HallazgoAuditoria> listarPorEstado(EstadoHallazgo estado);
}
