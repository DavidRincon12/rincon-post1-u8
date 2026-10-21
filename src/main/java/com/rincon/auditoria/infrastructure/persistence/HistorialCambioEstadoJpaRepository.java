package com.rincon.auditoria.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/**
 * Extiende el marcador {@link Repository} y no JpaRepository a propósito: así no se expone
 * ningún método de borrado ni de actualización, solo alta y consulta (append-only).
 */
interface HistorialCambioEstadoJpaRepository extends Repository<HistorialCambioEstadoJpaEntity, Long> {

    <S extends HistorialCambioEstadoJpaEntity> List<S> saveAll(Iterable<S> entidades);

    List<HistorialCambioEstadoJpaEntity> findByHallazgoIdOrderByIdAsc(UUID hallazgoId);
}
