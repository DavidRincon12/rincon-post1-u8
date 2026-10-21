package com.rincon.auditoria.usecase.port.out;

import com.rincon.auditoria.domain.model.EstadoHallazgo;
import com.rincon.auditoria.domain.model.HallazgoAuditoria;
import com.rincon.auditoria.domain.model.HallazgoId;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: lo que los casos de uso necesitan del almacenamiento de hallazgos.
 * Lo define el círculo interno y lo implementa la infraestructura (inversión de dependencias).
 */
public interface HallazgoRepositoryPort {

    HallazgoAuditoria guardar(HallazgoAuditoria hallazgo);

    Optional<HallazgoAuditoria> buscarPorId(HallazgoId id);

    List<HallazgoAuditoria> buscarTodos();

    List<HallazgoAuditoria> buscarPorEstado(EstadoHallazgo estado);
}
