package com.rincon.auditoria.infrastructure.persistence;

import com.rincon.auditoria.domain.model.EstadoHallazgo;
import com.rincon.auditoria.domain.model.Severidad;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface HallazgoJpaRepository extends JpaRepository<HallazgoJpaEntity, UUID> {

    List<HallazgoJpaEntity> findByEstadoOrderByFechaRegistroDesc(EstadoHallazgo estado);

    List<HallazgoJpaEntity> findAllByOrderByFechaRegistroDesc();

    @Query("SELECT h.estado AS estado, COUNT(h) AS total FROM HallazgoJpaEntity h GROUP BY h.estado")
    List<ConteoEstadoProyeccion> contarPorEstado();

    @Query("SELECT h.severidad AS severidad, COUNT(h) AS total FROM HallazgoJpaEntity h GROUP BY h.severidad")
    List<ConteoSeveridadProyeccion> contarPorSeveridad();

    @Query("""
            SELECT h.severidad AS severidad, AVG(h.horasHastaCierre) AS promedioHoras, COUNT(h) AS cerrados
            FROM HallazgoJpaEntity h
            WHERE h.horasHastaCierre IS NOT NULL
            GROUP BY h.severidad
            """)
    List<TiempoCierreProyeccion> promedioHorasCierrePorSeveridad();

    /** Proyecciones de interfaz: Spring Data las rellena directo desde las columnas agregadas. */
    interface ConteoEstadoProyeccion {
        EstadoHallazgo getEstado();

        Long getTotal();
    }

    interface ConteoSeveridadProyeccion {
        Severidad getSeveridad();

        Long getTotal();
    }

    interface TiempoCierreProyeccion {
        Severidad getSeveridad();

        Double getPromedioHoras();

        Long getCerrados();
    }
}
