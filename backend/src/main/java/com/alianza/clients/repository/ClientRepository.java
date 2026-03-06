package com.alianza.clients.repository;

import com.alianza.clients.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ClientRepository {

    private static final Logger log = LoggerFactory.getLogger(ClientRepository.class);

    private final Map<String, Client> almacenamiento = new ConcurrentHashMap<>();

    public ClientRepository() {
        cargarDatosIniciales();
    }

    private void cargarDatosIniciales() {
        List<Client> clientesIniciales = List.of(
            Client.builder().claveCompartida("jgutierrez").nombreCompleto("Juliana Gutierrez")
                .correoElectronico("jgutierrez@gmail.com").telefono("3219876543")
                .fechaRegistro(LocalDate.of(2019, 5, 20)).build(),
            Client.builder().claveCompartida("mmartinez").nombreCompleto("Manuel Martinez")
                .correoElectronico("mmartinez@gmail.com").telefono("3219876543")
                .fechaRegistro(LocalDate.of(2019, 5, 20)).build(),
            Client.builder().claveCompartida("aruiz").nombreCompleto("Ana Ruiz")
                .correoElectronico("aruiz@gmail.com").telefono("3219876543")
                .fechaRegistro(LocalDate.of(2019, 5, 20)).build(),
            Client.builder().claveCompartida("ogarcia").nombreCompleto("Oscar Garcia")
                .correoElectronico("ogarcia@gmail.com").telefono("3219876543")
                .fechaRegistro(LocalDate.of(2019, 5, 20)).build(),
            Client.builder().claveCompartida("tramos").nombreCompleto("Tania Ramos")
                .correoElectronico("tramos@gmail.com").telefono("3219876543")
                .fechaRegistro(LocalDate.of(2019, 5, 20)).build(),
            Client.builder().claveCompartida("cariza").nombreCompleto("Carlos Ariza")
                .correoElectronico("cariza@gmail.com").telefono("3219876543")
                .fechaRegistro(LocalDate.of(2019, 5, 20)).build(),
            Client.builder().claveCompartida("rvillaneda").nombreCompleto("Rodrigo Villaneda")
                .correoElectronico("rvillaneda@gmail.com").telefono("3219876543")
                .fechaRegistro(LocalDate.of(2019, 5, 20)).build(),
            Client.builder().claveCompartida("mfonseca").nombreCompleto("Mauricio Fonseca")
                .correoElectronico("mfonseca@gmail.com").telefono("3219876543")
                .fechaRegistro(LocalDate.of(2019, 5, 20)).build()
        );
        clientesIniciales.forEach(c -> almacenamiento.put(c.getClaveCompartida(), c));
        log.info("Repositorio inicializado con {} clientes de ejemplo", almacenamiento.size());
    }

    public List<Client> obtenerTodos() {
        log.debug("Consultando todos los clientes. Total: {}", almacenamiento.size());
        return new ArrayList<>(almacenamiento.values());
    }

    public Optional<Client> buscarPorClaveCompartida(String claveCompartida) {
        log.debug("Buscando cliente con clave: {}", claveCompartida);
        return Optional.ofNullable(almacenamiento.get(claveCompartida));
    }

    public List<Client> buscarPorClaveCompartidaParcial(String claveCompartida) {
        var claveLower = claveCompartida.toLowerCase();
        log.debug("Búsqueda parcial por clave: {}", claveLower);
        return almacenamiento.values().stream()
                .filter(c -> c.getClaveCompartida().toLowerCase().contains(claveLower))
                .collect(Collectors.toList());
    }

    public List<Client> busquedaAvanzada(String nombre, String telefono, String correo,
                                          LocalDate fechaDesde, LocalDate fechaHasta) {
        log.debug("Búsqueda avanzada — nombre={}, telefono={}, correo={}, desde={}, hasta={}",
                nombre, telefono, correo, fechaDesde, fechaHasta);
        return almacenamiento.values().stream()
                .filter(c -> esVacio(nombre)   || c.getNombreCompleto().toLowerCase().contains(nombre.toLowerCase()))
                .filter(c -> esVacio(telefono) || c.getTelefono().contains(telefono))
                .filter(c -> esVacio(correo)   || c.getCorreoElectronico().toLowerCase().contains(correo.toLowerCase()))
                .filter(c -> fechaDesde == null || c.getFechaRegistro() == null || !c.getFechaRegistro().isBefore(fechaDesde))
                .filter(c -> fechaHasta == null || c.getFechaRegistro() == null || !c.getFechaRegistro().isAfter(fechaHasta))
                .collect(Collectors.toList());
    }

    public Client guardar(Client cliente) {
        if (almacenamiento.containsKey(cliente.getClaveCompartida())) {
            throw new IllegalStateException("La clave compartida ya existe: " + cliente.getClaveCompartida());
        }
        almacenamiento.put(cliente.getClaveCompartida(), cliente);
        log.info("Cliente guardado con clave: {}", cliente.getClaveCompartida());
        return cliente;
    }

    public boolean existePorClaveCompartida(String claveCompartida) {
        return almacenamiento.containsKey(claveCompartida);
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
