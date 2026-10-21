package com.rincon.auditoria.infrastructure.persistence;

import com.rincon.auditoria.domain.model.EstadoHallazgo;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface HallazgoJpaRepository extends JpaRepository<HallazgoJpaEntity, UUID> {

    List<HallazgoJpaEntity> findByEstadoOrderByFechaRegistroDesc(EstadoHallazgo estado);

    List<HallazgoJpaEntity> findAllByOrderByFechaRegistroDesc();
}
