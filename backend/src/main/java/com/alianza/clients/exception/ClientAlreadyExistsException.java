package com.alianza.clients.exception;

public class ClientAlreadyExistsException extends RuntimeException {

    public ClientAlreadyExistsException(String mensaje) {
        super(mensaje);
    }
}
