package com.rincon.auditoria.infrastructure.persistence;

import com.rincon.auditoria.domain.model.EstadoHallazgo;
import com.rincon.auditoria.domain.model.HallazgoAuditoria;
import com.rincon.auditoria.domain.model.HallazgoId;
import com.rincon.auditoria.domain.model.PlanRemediacion;
import com.rincon.auditoria.usecase.dto.ConteoPorEstado;
import com.rincon.auditoria.usecase.dto.ConteoPorSeveridad;
import com.rincon.auditoria.usecase.dto.TiempoPromedioCierre;
import com.rincon.auditoria.usecase.port.out.HallazgoRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador de salida: traduce entre el agregado de dominio y la entidad JPA. */
@Component
class HallazgoRepositoryAdapter implements HallazgoRepositoryPort {

    private final HallazgoJpaRepository jpa;

    HallazgoRepositoryAdapter(HallazgoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public HallazgoAuditoria guardar(HallazgoAuditoria hallazgo) {
        // Se reutiliza la entidad gestionada si existe, para que @Version detecte escrituras concurrentes.
        HallazgoJpaEntity entidad = jpa.findById(hallazgo.getId().valor()).orElseGet(HallazgoJpaEntity::new);
        copiarAEntidad(hallazgo, entidad);
        return aDominio(jpa.saveAndFlush(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HallazgoAuditoria> buscarPorId(HallazgoId id) {
        return jpa.findById(id.valor()).map(HallazgoRepositoryAdapter::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallazgoAuditoria> buscarTodos() {
        return jpa.findAllByOrderByFechaRegistroDesc().stream().map(HallazgoRepositoryAdapter::aDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallazgoAuditoria> buscarPorEstado(EstadoHallazgo estado) {
        return jpa.findByEstadoOrderByFechaRegistroDesc(estado).stream()
                .map(HallazgoRepositoryAdapter::aDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConteoPorEstado> contarPorEstado() {
        return jpa.contarPorEstado().stream()
                .map(p -> new ConteoPorEstado(p.getEstado(), p.getTotal())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConteoPorSeveridad> contarPorSeveridad() {
        return jpa.contarPorSeveridad().stream()
                .map(p -> new ConteoPorSeveridad(p.getSeveridad(), p.getTotal())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TiempoPromedioCierre> promedioHorasCierrePorSeveridad() {
        return jpa.promedioHorasCierrePorSeveridad().stream()
                .map(p -> new TiempoPromedioCierre(p.getSeveridad(), p.getPromedioHoras(), p.getCerrados())).toList();
    }

    private static void copiarAEntidad(HallazgoAuditoria origen, HallazgoJpaEntity destino) {
        destino.id = origen.getId().valor();
        destino.titulo = origen.getTitulo();
        destino.descripcion = origen.getDescripcion();
        destino.severidad = origen.getSeveridad();
        destino.estado = origen.getEstado();
        destino.planResponsable = origen.getPlanRemediacion().map(PlanRemediacion::responsable).orElse(null);
        destino.planAccion = origen.getPlanRemediacion().map(PlanRemediacion::accion).orElse(null);
        destino.planFechaCompromiso = origen.getPlanRemediacion().map(PlanRemediacion::fechaCompromiso).orElse(null);
        destino.evidenciaRemediacion = origen.getEvidenciaRemediacion().orElse(null);
        destino.comentarioCierre = origen.getComentarioCierre().orElse(null);
        destino.fechaRegistro = origen.getFechaRegistro();
        destino.fechaActualizacion = origen.getFechaActualizacion();
        destino.fechaCierre = origen.getFechaCierre().orElse(null);
        destino.horasHastaCierre = origen.getHorasHastaCierre().orElse(null);
    }

    private static HallazgoAuditoria aDominio(HallazgoJpaEntity e) {
        PlanRemediacion plan = e.planResponsable == null
                ? null
                : new PlanRemediacion(e.planResponsable, e.planAccion, e.planFechaCompromiso);
        return HallazgoAuditoria.reconstruir(
                new HallazgoId(e.id), e.titulo, e.descripcion, e.severidad, e.estado, plan,
                e.evidenciaRemediacion, e.comentarioCierre,
                e.fechaRegistro, e.fechaActualizacion, e.fechaCierre, e.horasHastaCierre);
    }
}
