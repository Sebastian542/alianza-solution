package com.alianza.clients.exception;

public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(String mensaje) {
        super(mensaje);
    }
}
