package com.alianza.clients.controller;

import com.alianza.clients.exception.ClientNotFoundException;
import com.alianza.clients.model.Client;
import com.alianza.clients.model.ClientRequest;
import com.alianza.clients.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@DisplayName("Pruebas unitarias de ClientController")
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    private ObjectMapper objectMapper;
    private Client clienteEjemplo;

    @BeforeEach
    void configurar() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        clienteEjemplo = Client.builder()
                .claveCompartida("jgutierrez")
                .nombreCompleto("Juliana Gutierrez")
                .correoElectronico("jgutierrez@gmail.com")
                .telefono("3219876543")
                .fechaRegistro(LocalDate.of(2019, 5, 20))
                .build();
    }

    @Test
    @DisplayName("GET /api/clientes debe retornar 200 con la lista de clientes")
    void listarClientes_retorna200() throws Exception {
        when(clientService.obtenerTodosLosClientes()).thenReturn(List.of(clienteEjemplo));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].claveCompartida").value("jgutierrez"))
                .andExpect(jsonPath("$[0].nombreCompleto").value("Juliana Gutierrez"));
    }

    @Test
    @DisplayName("GET /api/clientes?clave=jgut debe retornar resultados filtrados")
    void listarClientes_conClave_retornaFiltrados() throws Exception {
        when(clientService.buscarPorClaveCompartida("jgut")).thenReturn(List.of(clienteEjemplo));

        mockMvc.perform(get("/api/clientes").param("clave", "jgut"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].claveCompartida").value("jgutierrez"));
    }

    @Test
    @DisplayName("GET /api/clientes/{clave} debe retornar 404 si el cliente no existe")
    void obtenerCliente_noExiste_retorna404() throws Exception {
        when(clientService.obtenerPorClaveCompartida("desconocido"))
                .thenThrow(new ClientNotFoundException("No encontrado"));

        mockMvc.perform(get("/api/clientes/desconocido"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/clientes debe retornar 201 con el cliente creado")
    void crearCliente_requestValido_retorna201() throws Exception {
        var request = new ClientRequest();
        request.setNombre("Juliana Gutierrez");
        request.setCorreoElectronico("jgutierrez@gmail.com");
        request.setTelefono("3219876543");

        when(clientService.crearCliente(any())).thenReturn(clienteEjemplo);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.claveCompartida").value("jgutierrez"));
    }

    @Test
    @DisplayName("POST /api/clientes debe retornar 400 cuando el request es inválido")
    void crearCliente_requestInvalido_retorna400() throws Exception {
        var request = new ClientRequest();
        request.setNombre("");
        request.setCorreoElectronico("no-es-un-correo");
        request.setTelefono("1");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erroresCampos").exists());
    }

    @Test
    @DisplayName("GET /api/clientes/exportar/csv debe retornar un archivo adjunto")
    void exportarCsv_retornaArchivoAdjunto() throws Exception {
        when(clientService.exportarCsv()).thenReturn("Clave Compartida,Nombre\njgutierrez,Juliana");

        mockMvc.perform(get("/api/clientes/exportar/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("clientes.csv")));
    }
}
