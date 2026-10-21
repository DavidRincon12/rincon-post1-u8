package com.rincon.auditoria.infrastructure.web;

import com.rincon.auditoria.domain.model.EstadoHallazgo;
import com.rincon.auditoria.domain.model.HallazgoAuditoria;
import com.rincon.auditoria.domain.model.HallazgoId;
import com.rincon.auditoria.infrastructure.web.dto.CerrarHallazgoRequest;
import com.rincon.auditoria.infrastructure.web.dto.EnviarVerificacionRequest;
import com.rincon.auditoria.infrastructure.web.dto.HallazgoResponse;
import com.rincon.auditoria.infrastructure.web.dto.IniciarRemediacionRequest;
import com.rincon.auditoria.infrastructure.web.dto.RechazarVerificacionRequest;
import com.rincon.auditoria.infrastructure.web.dto.RegistrarHallazgoRequest;
import com.rincon.auditoria.usecase.port.in.CerrarHallazgoUseCase;
import com.rincon.auditoria.usecase.port.in.ConsultarHallazgosUseCase;
import com.rincon.auditoria.usecase.port.in.EnviarAVerificacionUseCase;
import com.rincon.auditoria.usecase.port.in.IniciarRemediacionUseCase;
import com.rincon.auditoria.usecase.port.in.RechazarVerificacionUseCase;
import com.rincon.auditoria.usecase.port.in.RegistrarHallazgoUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Adaptador de entrada HTTP: solo traduce peticiones a comandos y resultados a DTO. */
@RestController
@RequestMapping("/api/v1/hallazgos")
public class HallazgoController {

    private final RegistrarHallazgoUseCase registrar;
    private final IniciarRemediacionUseCase iniciarRemediacion;
    private final EnviarAVerificacionUseCase enviarAVerificacion;
    private final RechazarVerificacionUseCase rechazarVerificacion;
    private final CerrarHallazgoUseCase cerrar;
    private final ConsultarHallazgosUseCase consultar;

    public HallazgoController(RegistrarHallazgoUseCase registrar,
                              IniciarRemediacionUseCase iniciarRemediacion,
                              EnviarAVerificacionUseCase enviarAVerificacion,
                              RechazarVerificacionUseCase rechazarVerificacion,
                              CerrarHallazgoUseCase cerrar,
                              ConsultarHallazgosUseCase consultar) {
        this.registrar = registrar;
        this.iniciarRemediacion = iniciarRemediacion;
        this.enviarAVerificacion = enviarAVerificacion;
        this.rechazarVerificacion = rechazarVerificacion;
        this.cerrar = cerrar;
        this.consultar = consultar;
    }

    @PostMapping
    public ResponseEntity<HallazgoResponse> registrar(@Valid @RequestBody RegistrarHallazgoRequest request) {
        HallazgoAuditoria creado = registrar.ejecutar(new RegistrarHallazgoUseCase.Comando(
                request.titulo(), request.descripcion(), request.severidad()));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getId().toString()).toUri();
        return ResponseEntity.created(ubicacion).body(HallazgoResponse.desde(creado));
    }

    @GetMapping
    public List<HallazgoResponse> listar(@RequestParam(required = false) EstadoHallazgo estado) {
        List<HallazgoAuditoria> resultado = estado == null ? consultar.listarTodos() : consultar.listarPorEstado(estado);
        return resultado.stream().map(HallazgoResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public HallazgoResponse obtener(@PathVariable String id) {
        return HallazgoResponse.desde(consultar.obtenerPorId(HallazgoId.de(id)));
    }

    @PostMapping("/{id}/remediacion")
    public HallazgoResponse iniciarRemediacion(@PathVariable String id,
                                               @Valid @RequestBody IniciarRemediacionRequest request) {
        return HallazgoResponse.desde(iniciarRemediacion.ejecutar(new IniciarRemediacionUseCase.Comando(
                HallazgoId.de(id), request.responsable(), request.accion(), request.fechaCompromiso())));
    }

    @PostMapping("/{id}/verificacion")
    public HallazgoResponse enviarAVerificacion(@PathVariable String id,
                                                @Valid @RequestBody EnviarVerificacionRequest request) {
        return HallazgoResponse.desde(enviarAVerificacion.ejecutar(
                new EnviarAVerificacionUseCase.Comando(HallazgoId.de(id), request.evidencia())));
    }

    @PostMapping("/{id}/rechazo")
    public HallazgoResponse rechazarVerificacion(@PathVariable String id,
                                                 @Valid @RequestBody RechazarVerificacionRequest request) {
        return HallazgoResponse.desde(rechazarVerificacion.ejecutar(
                new RechazarVerificacionUseCase.Comando(HallazgoId.de(id), request.motivo())));
    }

    @PostMapping("/{id}/cierre")
    public HallazgoResponse cerrar(@PathVariable String id, @Valid @RequestBody CerrarHallazgoRequest request) {
        return HallazgoResponse.desde(cerrar.ejecutar(
                new CerrarHallazgoUseCase.Comando(HallazgoId.de(id), request.comentarioCierre())));
    }
}
