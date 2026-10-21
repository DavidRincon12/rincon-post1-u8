package com.rincon.auditoria.infrastructure.persistence;

import com.rincon.auditoria.domain.model.EstadoHallazgo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

/**
 * Registro de bitácora. {@code @Immutable} hace que Hibernate ignore cualquier UPDATE sobre la
 * entidad y las columnas son {@code updatable = false}: una fila escrita no se vuelve a modificar.
 */
@Entity
@Immutable
@Table(name = "historial_cambio_estado", indexes = {
        @Index(name = "idx_historial_hallazgo", columnList = "hallazgo_id")
})
class HistorialCambioEstadoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    Long id;

    @Column(name = "hallazgo_id", nullable = false, updatable = false)
    UUID hallazgoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", updatable = false, length = 20)
    EstadoHallazgo estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, updatable = false, length = 20)
    EstadoHallazgo estadoNuevo;

    @Column(name = "ocurrido_en", nullable = false, updatable = false)
    Instant ocurridoEn;

    @Column(name = "detalle", updatable = false, length = 1500)
    String detalle;

    /** Requerido por JPA. */
    protected HistorialCambioEstadoJpaEntity() {
    }
}
