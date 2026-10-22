package com.rincon.auditoria.infrastructure.web;

import com.rincon.auditoria.domain.exception.DatosInvalidosException;
import com.rincon.auditoria.domain.exception.HallazgoNoEncontradoException;
import com.rincon.auditoria.domain.exception.PlanRemediacionInvalidoException;
import com.rincon.auditoria.domain.exception.TransicionInvalidaException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Único punto donde las excepciones se traducen a HTTP (RFC 9457, Problem Details).
 * Los casos de uso lanzan excepciones de dominio sin saber nada del protocolo.
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    @ExceptionHandler(TransicionInvalidaException.class)
    public ProblemDetail transicionInvalida(TransicionInvalidaException e) {
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Transición de estado inválida", e.getMessage());
        problema.setProperty("estadoOrigen", e.getOrigen());
        problema.setProperty("estadoDestino", e.getDestino());
        return problema;
    }

    @ExceptionHandler({DatosInvalidosException.class, PlanRemediacionInvalidoException.class})
    public ProblemDetail reglaDeNegocio(RuntimeException e) {
        return problema(HttpStatus.BAD_REQUEST, "Datos inválidos", e.getMessage());
    }

    @ExceptionHandler(HallazgoNoEncontradoException.class)
    public ProblemDetail noEncontrado(HallazgoNoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "Hallazgo no encontrado", e.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail conflictoDeConcurrencia(OptimisticLockingFailureException e) {
        LOG.warn("Conflicto de concurrencia optimista: {}", e.getMessage());
        return problema(HttpStatus.CONFLICT, "Conflicto de concurrencia",
                "El hallazgo fue modificado por otra operación; vuelva a consultarlo y reintente");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> errores.putIfAbsent(f.getField(), String.valueOf(f.getDefaultMessage())));
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Solicitud inválida",
                "Uno o más campos no cumplen las validaciones");
        problema.setProperty("errors", errores);
        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail cuerpoIlegible(HttpMessageNotReadableException e) {
        return problema(HttpStatus.BAD_REQUEST, "Cuerpo de la solicitud ilegible",
                "El JSON está mal formado o contiene un valor no permitido (enum o fecha con formato incorrecto)");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail parametroInvalido(MethodArgumentTypeMismatchException e) {
        return problema(HttpStatus.BAD_REQUEST, "Parámetro inválido",
                "El valor '" + e.getValue() + "' no es válido para el parámetro '" + e.getName() + "'");
    }

    /** Red de seguridad: se registra el detalle en el servidor y al cliente no se le expone nada interno. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception e) {
        LOG.error("Error no controlado", e);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado");
    }

    private static ProblemDetail problema(HttpStatus estado, String titulo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        return problema;
    }
}
