package com.rincon.auditoria.domain.model;

import com.rincon.auditoria.domain.exception.DatosInvalidosException;
import com.rincon.auditoria.domain.exception.PlanRemediacionInvalidoException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root del módulo. Todas las reglas del ciclo de vida del hallazgo viven aquí:
 * ningún cambio de estado se hace desde fuera, así las invariantes no se pueden saltar.
 * El tiempo entra por parámetro para que el dominio sea determinista y fácil de probar.
 */
public final class HallazgoAuditoria {

    public static final int TITULO_MIN = 5;
    public static final int TITULO_MAX = 150;
    public static final int DESCRIPCION_MIN = 10;
    public static final int DESCRIPCION_MAX = 2000;
    public static final int TEXTO_CIERRE_MIN = 5;
    public static final int TEXTO_CIERRE_MAX = 1000;

    private final HallazgoId id;
    private final String titulo;
    private final String descripcion;
    private final Severidad severidad;
    private final Instant fechaRegistro;

    private EstadoHallazgo estado;
    private PlanRemediacion planRemediacion;
    private String evidenciaRemediacion;
    private String comentarioCierre;
    private Instant fechaActualizacion;
    private Instant fechaCierre;
    private Long horasHastaCierre;

    private HallazgoAuditoria(HallazgoId id, String titulo, String descripcion, Severidad severidad,
                              EstadoHallazgo estado, PlanRemediacion planRemediacion,
                              String evidenciaRemediacion, String comentarioCierre,
                              Instant fechaRegistro, Instant fechaActualizacion,
                              Instant fechaCierre, Long horasHastaCierre) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.severidad = severidad;
        this.estado = estado;
        this.planRemediacion = planRemediacion;
        this.evidenciaRemediacion = evidenciaRemediacion;
        this.comentarioCierre = comentarioCierre;
        this.fechaRegistro = fechaRegistro;
        this.fechaActualizacion = fechaActualizacion;
        this.fechaCierre = fechaCierre;
        this.horasHastaCierre = horasHastaCierre;
    }

    /** Crea un hallazgo nuevo; siempre nace en estado ABIERTO. */
    public static HallazgoAuditoria registrar(String titulo, String descripcion, Severidad severidad, Instant ahora) {
        if (severidad == null) {
            throw new DatosInvalidosException("El campo 'severidad' es obligatorio");
        }
        Instant momento = requerirInstante(ahora);
        return new HallazgoAuditoria(
                HallazgoId.nuevo(),
                Texto.requerir(titulo, "titulo", TITULO_MIN, TITULO_MAX),
                Texto.requerir(descripcion, "descripcion", DESCRIPCION_MIN, DESCRIPCION_MAX),
                severidad, EstadoHallazgo.ABIERTO, null, null, null,
                momento, momento, null, null);
    }

    /** Rehidrata un hallazgo ya persistido sin volver a ejecutar las reglas de creación. */
    public static HallazgoAuditoria reconstruir(HallazgoId id, String titulo, String descripcion, Severidad severidad,
                                                EstadoHallazgo estado, PlanRemediacion planRemediacion,
                                                String evidenciaRemediacion, String comentarioCierre,
                                                Instant fechaRegistro, Instant fechaActualizacion,
                                                Instant fechaCierre, Long horasHastaCierre) {
        return new HallazgoAuditoria(
                Objects.requireNonNull(id, "id"), Objects.requireNonNull(titulo, "titulo"),
                Objects.requireNonNull(descripcion, "descripcion"), Objects.requireNonNull(severidad, "severidad"),
                Objects.requireNonNull(estado, "estado"), planRemediacion, evidenciaRemediacion, comentarioCierre,
                Objects.requireNonNull(fechaRegistro, "fechaRegistro"),
                Objects.requireNonNull(fechaActualizacion, "fechaActualizacion"),
                fechaCierre, horasHastaCierre);
    }

    /** ABIERTO → EN_REMEDIACION. El plan debe comprometer una fecha dentro del SLA de la severidad. */
    public void iniciarRemediacion(PlanRemediacion plan, Instant ahora) {
        estado.validarTransicionA(EstadoHallazgo.EN_REMEDIACION);
        if (plan == null) {
            throw new DatosInvalidosException("El plan de remediación es obligatorio");
        }
        Instant momento = requerirInstante(ahora);
        validarPlazo(plan, momento);
        this.planRemediacion = plan;
        aplicarEstado(EstadoHallazgo.EN_REMEDIACION, momento);
    }

    /** EN_REMEDIACION → EN_VERIFICACION. Exige evidencia de que la corrección se ejecutó. */
    public void enviarAVerificacion(String evidencia, Instant ahora) {
        estado.validarTransicionA(EstadoHallazgo.EN_VERIFICACION);
        String evidenciaLimpia = Texto.requerir(evidencia, "evidencia", TEXTO_CIERRE_MIN, TEXTO_CIERRE_MAX);
        this.evidenciaRemediacion = evidenciaLimpia;
        aplicarEstado(EstadoHallazgo.EN_VERIFICACION, requerirInstante(ahora));
    }

    /** EN_VERIFICACION → EN_REMEDIACION. La verificación falló: se descarta la evidencia y se corrige de nuevo. */
    public void rechazarVerificacion(String motivo, Instant ahora) {
        estado.validarTransicionA(EstadoHallazgo.EN_REMEDIACION);
        Texto.requerir(motivo, "motivo", TEXTO_CIERRE_MIN, TEXTO_CIERRE_MAX);
        this.evidenciaRemediacion = null;
        aplicarEstado(EstadoHallazgo.EN_REMEDIACION, requerirInstante(ahora));
    }

    /** EN_VERIFICACION → CERRADO. Registra el comentario de cierre y el tiempo total empleado. */
    public void cerrar(String comentario, Instant ahora) {
        estado.validarTransicionA(EstadoHallazgo.CERRADO);
        String comentarioLimpio = Texto.requerir(comentario, "comentarioCierre", TEXTO_CIERRE_MIN, TEXTO_CIERRE_MAX);
        Instant momento = requerirInstante(ahora);
        this.comentarioCierre = comentarioLimpio;
        this.fechaCierre = momento;
        this.horasHastaCierre = Math.max(0L, Duration.between(fechaRegistro, momento).toHours());
        aplicarEstado(EstadoHallazgo.CERRADO, momento);
    }

    private void validarPlazo(PlanRemediacion plan, Instant ahora) {
        LocalDate hoy = LocalDate.ofInstant(ahora, ZoneOffset.UTC);
        LocalDate limite = hoy.plusDays(severidad.diasPlazoRemediacion());
        if (plan.fechaCompromiso().isBefore(hoy)) {
            throw new PlanRemediacionInvalidoException(
                    "La fecha de compromiso " + plan.fechaCompromiso() + " no puede estar en el pasado");
        }
        if (plan.fechaCompromiso().isAfter(limite)) {
            throw new PlanRemediacionInvalidoException(String.format(
                    "Un hallazgo de severidad %s exige remediar en máximo %d días (límite %s); se propuso %s",
                    severidad, severidad.diasPlazoRemediacion(), limite, plan.fechaCompromiso()));
        }
    }

    private void aplicarEstado(EstadoHallazgo nuevo, Instant momento) {
        this.estado = nuevo;
        this.fechaActualizacion = momento;
    }

    private static Instant requerirInstante(Instant ahora) {
        if (ahora == null) {
            throw new DatosInvalidosException("La marca de tiempo de la operación es obligatoria");
        }
        return ahora;
    }

    public HallazgoId getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Severidad getSeveridad() {
        return severidad;
    }

    public EstadoHallazgo getEstado() {
        return estado;
    }

    public Optional<PlanRemediacion> getPlanRemediacion() {
        return Optional.ofNullable(planRemediacion);
    }

    public Optional<String> getEvidenciaRemediacion() {
        return Optional.ofNullable(evidenciaRemediacion);
    }

    public Optional<String> getComentarioCierre() {
        return Optional.ofNullable(comentarioCierre);
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public Instant getFechaActualizacion() {
        return fechaActualizacion;
    }

    public Optional<Instant> getFechaCierre() {
        return Optional.ofNullable(fechaCierre);
    }

    public Optional<Long> getHorasHastaCierre() {
        return Optional.ofNullable(horasHastaCierre);
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof HallazgoAuditoria)) {
            return false;
        }
        return id.equals(((HallazgoAuditoria) otro).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "HallazgoAuditoria[" + id + ", " + severidad + ", " + estado + "]";
    }
}
