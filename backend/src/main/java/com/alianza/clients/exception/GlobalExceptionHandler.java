package com.alianza.clients.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<RespuestaError> manejarClienteNoEncontrado(ClientNotFoundException ex) {
        log.warn("Cliente no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new RespuestaError(404, ex.getMessage()));
    }

    @ExceptionHandler(ClientAlreadyExistsException.class)
    public ResponseEntity<RespuestaError> manejarClienteYaExiste(ClientAlreadyExistsException ex) {
        log.warn("Conflicto al crear cliente: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new RespuestaError(409, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> erroresCampos = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            var campo = ((FieldError) error).getField();
            erroresCampos.put(campo, error.getDefaultMessage());
        });
        log.warn("Error de validación: {}", erroresCampos);
        var respuesta = new RespuestaError(400, "Los datos enviados no son válidos");
        respuesta.setErroresCampos(erroresCampos);
        return ResponseEntity.badRequest().body(respuesta);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarErrorGeneral(Exception ex) {
        log.error("Error inesperado en el servidor", ex);
        return ResponseEntity.internalServerError()
                .body(new RespuestaError(500, "Ocurrió un error interno en el servidor"));
    }

    public static class RespuestaError {

        private final int estado;
        private final String mensaje;
        private final LocalDateTime marca = LocalDateTime.now();
        private Map<String, String> erroresCampos;

        public RespuestaError(int estado, String mensaje) {
            this.estado = estado;
            this.mensaje = mensaje;
        }

        public int getEstado() { return estado; }
        public String getMensaje() { return mensaje; }
        public LocalDateTime getMarca() { return marca; }
        public Map<String, String> getErroresCampos() { return erroresCampos; }
        public void setErroresCampos(Map<String, String> erroresCampos) {
            this.erroresCampos = erroresCampos;
        }
    }
}
