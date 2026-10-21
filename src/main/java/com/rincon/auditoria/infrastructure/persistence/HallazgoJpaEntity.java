package com.rincon.auditoria.infrastructure.persistence;

import com.rincon.auditoria.domain.model.EstadoHallazgo;
import com.rincon.auditoria.domain.model.Severidad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Modelo de persistencia, separado del agregado de dominio a propósito: el esquema puede
 * cambiar sin tocar las reglas de negocio. Los campos son visibles solo dentro del paquete,
 * donde únicamente el adaptador los lee y escribe.
 */
@Entity
@Table(name = "hallazgos_auditoria", indexes = {
        @Index(name = "idx_hallazgo_estado", columnList = "estado"),
        @Index(name = "idx_hallazgo_severidad", columnList = "severidad")
})
class HallazgoJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    UUID id;

    @Version
    @Column(name = "version", nullable = false)
    Long version;

    @Column(name = "titulo", nullable = false, length = 150)
    String titulo;

    @Column(name = "descripcion", nullable = false, length = 2000)
    String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "severidad", nullable = false, length = 20)
    Severidad severidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    EstadoHallazgo estado;

    @Column(name = "plan_responsable", length = 100)
    String planResponsable;

    @Column(name = "plan_accion", length = 1000)
    String planAccion;

    @Column(name = "plan_fecha_compromiso")
    LocalDate planFechaCompromiso;

    @Column(name = "evidencia_remediacion", length = 1000)
    String evidenciaRemediacion;

    @Column(name = "comentario_cierre", length = 1000)
    String comentarioCierre;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    Instant fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    Instant fechaActualizacion;

    @Column(name = "fecha_cierre")
    Instant fechaCierre;

    @Column(name = "horas_hasta_cierre")
    Long horasHastaCierre;

    /** Requerido por JPA. */
    protected HallazgoJpaEntity() {
    }
}
