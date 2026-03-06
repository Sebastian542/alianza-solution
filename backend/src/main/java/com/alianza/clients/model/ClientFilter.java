package com.alianza.clients.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ClientFilter {

    private String nombre;
    private String telefono;
    private String correoElectronico;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
}
