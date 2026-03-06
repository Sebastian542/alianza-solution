package com.alianza.clients.service;

import com.alianza.clients.exception.ClientAlreadyExistsException;
import com.alianza.clients.exception.ClientNotFoundException;
import com.alianza.clients.model.Client;
import com.alianza.clients.model.ClientFilter;
import com.alianza.clients.model.ClientRequest;
import com.alianza.clients.repository.ClientRepository;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;

@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> obtenerTodosLosClientes() {
        log.info("Consultando todos los clientes");
        var clientes = clientRepository.obtenerTodos();
        log.info("Total de clientes encontrados: {}", clientes.size());
        return clientes;
    }

    public List<Client> buscarPorClaveCompartida(String claveCompartida) {
        log.info("Búsqueda simple por clave compartida: '{}'", claveCompartida);
        if (claveCompartida == null || claveCompartida.isBlank()) {
            return obtenerTodosLosClientes();
        }
        var resultados = clientRepository.buscarPorClaveCompartidaParcial(claveCompartida);
        log.info("Búsqueda completada: {} resultado(s) para '{}'", resultados.size(), claveCompartida);
        return resultados;
    }

    public List<Client> busquedaAvanzada(ClientFilter filtro) {
        log.info("Ejecutando búsqueda avanzada con filtros: {}", filtro);
        var resultados = clientRepository.busquedaAvanzada(
                filtro.getNombre(),
                filtro.getTelefono(),
                filtro.getCorreoElectronico(),
                filtro.getFechaDesde(),
                filtro.getFechaHasta()
        );
        log.info("Búsqueda avanzada completada: {} resultado(s)", resultados.size());
        return resultados;
    }

    public Client crearCliente(ClientRequest request) {
        log.info("Creando nuevo cliente con nombre: '{}'", request.getNombre());
        var claveCompartida = generarClaveCompartida(request.getNombre());
        log.debug("Clave compartida generada: '{}'", claveCompartida);

        if (clientRepository.existePorClaveCompartida(claveCompartida)) {
            log.warn("Ya existe un cliente con la clave compartida: '{}'", claveCompartida);
            throw new ClientAlreadyExistsException(
                "Ya existe un cliente con la clave compartida '" + claveCompartida + "'"
            );
        }

        var nuevoCliente = Client.builder()
                .claveCompartida(claveCompartida)
                .nombreCompleto(request.getNombre())
                .correoElectronico(request.getCorreoElectronico())
                .telefono(request.getTelefono())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .fechaRegistro(LocalDate.now())
                .build();

        var clienteGuardado = clientRepository.guardar(nuevoCliente);
        log.info("Cliente creado exitosamente con clave: '{}'", clienteGuardado.getClaveCompartida());
        return clienteGuardado;
    }

    public Client obtenerPorClaveCompartida(String claveCompartida) {
        log.info("Consultando cliente con clave compartida: '{}'", claveCompartida);
        return clientRepository.buscarPorClaveCompartida(claveCompartida)
                .orElseThrow(() -> {
                    log.warn("No se encontró cliente con clave: '{}'", claveCompartida);
                    return new ClientNotFoundException(
                        "No se encontró un cliente con la clave '" + claveCompartida + "'"
                    );
                });
    }

    public String exportarCsv() {
        log.info("Iniciando exportación de clientes a CSV");
        var clientes = clientRepository.obtenerTodos();
        var escritor = new StringWriter();

        try (var csv = new CSVWriter(escritor)) {
            csv.writeNext(new String[]{
                "Clave Compartida", "Nombre Completo", "Correo Electrónico",
                "Teléfono", "Fecha Registro", "Fecha Inicio", "Fecha Fin"
            });
            for (var cliente : clientes) {
                csv.writeNext(new String[]{
                    cliente.getClaveCompartida(),
                    cliente.getNombreCompleto(),
                    cliente.getCorreoElectronico(),
                    cliente.getTelefono(),
                    cliente.getFechaRegistro() != null ? cliente.getFechaRegistro().toString() : "",
                    cliente.getFechaInicio()   != null ? cliente.getFechaInicio().toString()   : "",
                    cliente.getFechaFin()      != null ? cliente.getFechaFin().toString()      : ""
                });
            }
        } catch (Exception e) {
            log.error("Error al generar la exportación CSV", e);
            throw new RuntimeException("No se pudo generar el archivo CSV", e);
        }

        log.info("Exportación CSV completada: {} cliente(s)", clientes.size());
        return escritor.toString();
    }

    private String generarClaveCompartida(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "cliente" + System.currentTimeMillis();
        }
        var partes = nombre.trim().toLowerCase().split("\\s+");
        return partes.length == 1 ? partes[0] : partes[0].charAt(0) + partes[partes.length - 1];
    }
}
