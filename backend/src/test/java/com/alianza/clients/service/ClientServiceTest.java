package com.alianza.clients.service;

import com.alianza.clients.exception.ClientAlreadyExistsException;
import com.alianza.clients.exception.ClientNotFoundException;
import com.alianza.clients.model.Client;
import com.alianza.clients.model.ClientFilter;
import com.alianza.clients.model.ClientRequest;
import com.alianza.clients.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de ClientService")
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client clienteEjemplo;
    private ClientRequest requestEjemplo;

    @BeforeEach
    void configurar() {
        clienteEjemplo = Client.builder()
                .claveCompartida("jgutierrez")
                .nombreCompleto("Juliana Gutierrez")
                .correoElectronico("jgutierrez@gmail.com")
                .telefono("3219876543")
                .fechaRegistro(LocalDate.of(2019, 5, 20))
                .build();

        requestEjemplo = new ClientRequest();
        requestEjemplo.setNombre("Juliana Gutierrez");
        requestEjemplo.setCorreoElectronico("jgutierrez@gmail.com");
        requestEjemplo.setTelefono("3219876543");
    }

    @Test
    @DisplayName("Debe retornar todos los clientes del repositorio")
    void obtenerTodos_retornaTodosLosClientes() {
        when(clientRepository.obtenerTodos()).thenReturn(List.of(clienteEjemplo));

        var resultado = clientService.obtenerTodosLosClientes();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getClaveCompartida()).isEqualTo("jgutierrez");
        verify(clientRepository).obtenerTodos();
    }

    @Test
    @DisplayName("Debe retornar lista vacía si no hay clientes")
    void obtenerTodos_repositorioVacio_retornaListaVacia() {
        when(clientRepository.obtenerTodos()).thenReturn(List.of());

        assertThat(clientService.obtenerTodosLosClientes()).isEmpty();
    }

    @Test
    @DisplayName("Debe delegar al repositorio cuando la clave no está vacía")
    void buscarPorClave_claveValida_delegaAlRepositorio() {
        when(clientRepository.buscarPorClaveCompartidaParcial("jgut"))
                .thenReturn(List.of(clienteEjemplo));

        var resultado = clientService.buscarPorClaveCompartida("jgut");

        assertThat(resultado).hasSize(1);
        verify(clientRepository).buscarPorClaveCompartidaParcial("jgut");
        verify(clientRepository, never()).obtenerTodos();
    }

    @Test
    @DisplayName("Debe retornar todos los clientes cuando la clave está vacía")
    void buscarPorClave_claveVacia_retornaTodos() {
        when(clientRepository.obtenerTodos()).thenReturn(List.of(clienteEjemplo));

        clientService.buscarPorClaveCompartida("   ");

        verify(clientRepository).obtenerTodos();
        verify(clientRepository, never()).buscarPorClaveCompartidaParcial(any());
    }

    @Test
    @DisplayName("Debe guardar y retornar el cliente creado exitosamente")
    void crearCliente_datosValidos_creaExitosamente() {
        when(clientRepository.existePorClaveCompartida("jgutierrez")).thenReturn(false);
        when(clientRepository.guardar(any())).thenReturn(clienteEjemplo);

        var resultado = clientService.crearCliente(requestEjemplo);

        assertThat(resultado.getClaveCompartida()).isEqualTo("jgutierrez");
        verify(clientRepository).guardar(any(Client.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si la clave compartida ya existe")
    void crearCliente_claveRepetida_lanzaExcepcion() {
        when(clientRepository.existePorClaveCompartida("jgutierrez")).thenReturn(true);

        assertThatThrownBy(() -> clientService.crearCliente(requestEjemplo))
                .isInstanceOf(ClientAlreadyExistsException.class)
                .hasMessageContaining("jgutierrez");

        verify(clientRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe retornar el cliente si la clave compartida existe")
    void obtenerPorClave_clienteExiste_retornaCliente() {
        when(clientRepository.buscarPorClaveCompartida("jgutierrez"))
                .thenReturn(Optional.of(clienteEjemplo));

        assertThat(clientService.obtenerPorClaveCompartida("jgutierrez")
                .getClaveCompartida()).isEqualTo("jgutierrez");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el cliente no existe")
    void obtenerPorClave_clienteNoExiste_lanzaExcepcion() {
        when(clientRepository.buscarPorClaveCompartida("desconocido"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.obtenerPorClaveCompartida("desconocido"))
                .isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    @DisplayName("Debe pasar los filtros correctamente al repositorio")
    void busquedaAvanzada_pasaFiltrosAlRepositorio() {
        var filtro = new ClientFilter();
        filtro.setNombre("Juliana");
        when(clientRepository.busquedaAvanzada(any(), any(), any(), any(), any()))
                .thenReturn(List.of(clienteEjemplo));

        var resultado = clientService.busquedaAvanzada(filtro);

        assertThat(resultado).hasSize(1);
        verify(clientRepository).busquedaAvanzada("Juliana", null, null, null, null);
    }

    @Test
    @DisplayName("Debe retornar un CSV con encabezados y datos de los clientes")
    void exportarCsv_retornaCsvValido() {
        when(clientRepository.obtenerTodos()).thenReturn(List.of(clienteEjemplo));

        var csv = clientService.exportarCsv();

        assertThat(csv).contains("Clave Compartida").contains("jgutierrez");
    }
}
