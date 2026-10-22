package com.rincon.auditoria.soporte;

import com.rincon.auditoria.domain.model.CambioEstado;
import com.rincon.auditoria.domain.model.EstadoHallazgo;
import com.rincon.auditoria.domain.model.HallazgoAuditoria;
import com.rincon.auditoria.domain.model.HallazgoId;
import com.rincon.auditoria.domain.model.Severidad;
import com.rincon.auditoria.usecase.dto.ConteoPorEstado;
import com.rincon.auditoria.usecase.dto.ConteoPorSeveridad;
import com.rincon.auditoria.usecase.dto.TiempoPromedioCierre;
import com.rincon.auditoria.usecase.port.out.HallazgoRepositoryPort;
import com.rincon.auditoria.usecase.port.out.HistorialAuditoriaPort;
import com.rincon.auditoria.usecase.port.out.UnidadDeTrabajoPort;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Implementaciones en memoria de los puertos: permiten probar casos de uso sin Spring ni base de datos. */
public final class Fakes {

    private Fakes() {
    }

    /** Reloj controlable para avanzar el tiempo entre operaciones. */
    public static final class RelojMutable extends Clock {

        private Instant ahora;

        public RelojMutable(Instant inicio) {
            this.ahora = inicio;
        }

        public void avanzar(Duration duracion) {
            ahora = ahora.plus(duracion);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }

    public static final class UnidadDeTrabajoDirecta implements UnidadDeTrabajoPort {

        @Override
        public <T> T ejecutar(Supplier<T> operacion) {
            return operacion.get();
        }
    }

    public static final class HistorialEnMemoria implements HistorialAuditoriaPort {

        private final List<CambioEstado> cambios = new ArrayList<>();

        @Override
        public void registrar(List<CambioEstado> nuevos) {
            cambios.addAll(nuevos);
        }

        @Override
        public List<CambioEstado> buscarPorHallazgo(HallazgoId hallazgoId) {
            return cambios.stream().filter(c -> c.hallazgoId().equals(hallazgoId)).toList();
        }

        public int total() {
            return cambios.size();
        }
    }

    public static final class RepositorioEnMemoria implements HallazgoRepositoryPort {

        private final Map<HallazgoId, HallazgoAuditoria> almacen = new LinkedHashMap<>();

        @Override
        public HallazgoAuditoria guardar(HallazgoAuditoria hallazgo) {
            almacen.put(hallazgo.getId(), hallazgo);
            return hallazgo;
        }

        @Override
        public Optional<HallazgoAuditoria> buscarPorId(HallazgoId id) {
            return Optional.ofNullable(almacen.get(id));
        }

        @Override
        public List<HallazgoAuditoria> buscarTodos() {
            return almacen.values().stream()
                    .sorted(Comparator.comparing(HallazgoAuditoria::getFechaRegistro).reversed()).toList();
        }

        @Override
        public List<HallazgoAuditoria> buscarPorEstado(EstadoHallazgo estado) {
            return buscarTodos().stream().filter(h -> h.getEstado() == estado).toList();
        }

        @Override
        public List<ConteoPorEstado> contarPorEstado() {
            return almacen.values().stream()
                    .collect(Collectors.groupingBy(HallazgoAuditoria::getEstado, Collectors.counting()))
                    .entrySet().stream().map(e -> new ConteoPorEstado(e.getKey(), e.getValue())).toList();
        }

        @Override
        public List<ConteoPorSeveridad> contarPorSeveridad() {
            return almacen.values().stream()
                    .collect(Collectors.groupingBy(HallazgoAuditoria::getSeveridad, Collectors.counting()))
                    .entrySet().stream().map(e -> new ConteoPorSeveridad(e.getKey(), e.getValue())).toList();
        }

        @Override
        public List<TiempoPromedioCierre> promedioHorasCierrePorSeveridad() {
            Map<Severidad, List<Long>> horas = almacen.values().stream()
                    .filter(h -> h.getHorasHastaCierre().isPresent())
                    .collect(Collectors.groupingBy(HallazgoAuditoria::getSeveridad,
                            Collectors.mapping(h -> h.getHorasHastaCierre().orElseThrow(), Collectors.toList())));
            return horas.entrySet().stream().map(e -> new TiempoPromedioCierre(e.getKey(),
                    e.getValue().stream().mapToLong(Long::longValue).average().orElse(0.0),
                    e.getValue().size())).toList();
        }
    }
}
