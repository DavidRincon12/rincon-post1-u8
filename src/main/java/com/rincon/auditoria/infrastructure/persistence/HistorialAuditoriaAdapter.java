package com.rincon.auditoria.infrastructure.persistence;

import com.rincon.auditoria.domain.model.CambioEstado;
import com.rincon.auditoria.domain.model.HallazgoId;
import com.rincon.auditoria.usecase.port.out.HistorialAuditoriaPort;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador de la bitácora: solo inserta y lee filas, nunca las modifica. */
@Component
class HistorialAuditoriaAdapter implements HistorialAuditoriaPort {

    private final HistorialCambioEstadoJpaRepository jpa;

    HistorialAuditoriaAdapter(HistorialCambioEstadoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public void registrar(List<CambioEstado> cambios) {
        if (cambios.isEmpty()) {
            return;
        }
        jpa.saveAll(cambios.stream().map(HistorialAuditoriaAdapter::aEntidad).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CambioEstado> buscarPorHallazgo(HallazgoId hallazgoId) {
        return jpa.findByHallazgoIdOrderByIdAsc(hallazgoId.valor()).stream()
                .map(HistorialAuditoriaAdapter::aDominio).toList();
    }

    private static HistorialCambioEstadoJpaEntity aEntidad(CambioEstado c) {
        HistorialCambioEstadoJpaEntity e = new HistorialCambioEstadoJpaEntity();
        e.hallazgoId = c.hallazgoId().valor();
        e.estadoAnterior = c.estadoAnterior();
        e.estadoNuevo = c.estadoNuevo();
        e.ocurridoEn = c.ocurridoEn();
        e.detalle = c.detalle();
        return e;
    }

    private static CambioEstado aDominio(HistorialCambioEstadoJpaEntity e) {
        return new CambioEstado(new HallazgoId(e.hallazgoId), e.estadoAnterior, e.estadoNuevo, e.ocurridoEn, e.detalle);
    }
}
