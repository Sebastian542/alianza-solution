package com.alianza.clients.controller;

import com.alianza.clients.model.Client;
import com.alianza.clients.model.ClientFilter;
import com.alianza.clients.model.ClientRequest;
import com.alianza.clients.service.ClientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClientController {

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<List<Client>> listarClientes(
            @RequestParam(required = false) String clave) {
        log.info("GET /api/clientes — clave='{}'", clave);
        var resultado = (clave != null && !clave.isBlank())
                ? clientService.buscarPorClaveCompartida(clave)
                : clientService.obtenerTodosLosClientes();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{clave}")
    public ResponseEntity<Client> obtenerCliente(@PathVariable String clave) {
        log.info("GET /api/clientes/{}", clave);
        return ResponseEntity.ok(clientService.obtenerPorClaveCompartida(clave));
    }

    @PostMapping
    public ResponseEntity<Client> crearCliente(@Valid @RequestBody ClientRequest request) {
        log.info("POST /api/clientes — nombre='{}'", request.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.crearCliente(request));
    }

    @PostMapping("/busqueda")
    public ResponseEntity<List<Client>> busquedaAvanzada(@RequestBody ClientFilter filtro) {
        log.info("POST /api/clientes/busqueda");
        return ResponseEntity.ok(clientService.busquedaAvanzada(filtro));
    }

    @GetMapping("/exportar/csv")
    public ResponseEntity<byte[]> exportarCsv() {
        log.info("GET /api/clientes/exportar/csv");
        var csv = clientService.exportarCsv();
        var cabeceras = new HttpHeaders();
        cabeceras.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        cabeceras.setContentDispositionFormData("attachment", "clientes.csv");
        return ResponseEntity.ok().headers(cabeceras).body(csv.getBytes());
    }
}
