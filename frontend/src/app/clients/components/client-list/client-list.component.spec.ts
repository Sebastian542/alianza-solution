import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { ClientListComponent } from './client-list.component';
import { ClienteService } from '../../services/client.service';
import { Cliente } from '../../models/client.model';

describe('ClientListComponent', () => {
  let componente: ClientListComponent;
  let fixture: ComponentFixture<ClientListComponent>;
  let servicioMock: jasmine.SpyObj<ClienteService>;

  const clientesMock: Cliente[] = [
    { claveCompartida: 'jgutierrez', nombreCompleto: 'Juliana Gutierrez', correoElectronico: 'j@gmail.com', telefono: '3219876543', fechaRegistro: '2019-05-20' }
  ];

  beforeEach(async () => {
    servicioMock = jasmine.createSpyObj('ClienteService', [
      'obtenerTodos', 'buscarPorClave', 'crearCliente', 'busquedaAvanzada', 'exportarCsv'
    ]);
    servicioMock.obtenerTodos.and.returnValue(of(clientesMock));

    await TestBed.configureTestingModule({
      imports: [ClientListComponent, ReactiveFormsModule, FormsModule],
      providers: [{ provide: ClienteService, useValue: servicioMock }]
    }).compileComponents();

    fixture    = TestBed.createComponent(ClientListComponent);
    componente = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debe crearse correctamente', () => expect(componente).toBeTruthy());

  it('debe cargar los clientes al inicializarse', () => {
    expect(servicioMock.obtenerTodos).toHaveBeenCalled();
    expect(componente.clientes().length).toBe(1);
  });

  it('el signal totalClientes debe reflejar la cantidad de clientes cargados', () => {
    expect(componente.totalClientes()).toBe(1);
  });

  it('debe mostrar mensaje de error cuando falla la carga', fakeAsync(() => {
    servicioMock.obtenerTodos.and.returnValue(throwError(() => new Error('Error de servidor')));
    componente.cargarClientes();
    tick();
    expect(componente.mensajeError()).toBeTruthy();
  }));

  describe('Búsqueda simple', () => {
    it('debe llamar buscarPorClave cuando la clave no está vacía', () => {
      servicioMock.buscarPorClave.and.returnValue(of(clientesMock));
      componente.claveSimple = 'jgut';
      componente.onBusquedaSimple();
      expect(servicioMock.buscarPorClave).toHaveBeenCalledWith('jgut');
    });

    it('debe recargar todos los clientes cuando la clave está vacía', () => {
      componente.claveSimple = '';
      componente.onBusquedaSimple();
      expect(servicioMock.obtenerTodos).toHaveBeenCalledTimes(2);
    });
  });

  describe('Signal mostrarBusquedaAvanzada', () => {
    it('debe alternar correctamente', () => {
      expect(componente.mostrarBusquedaAvanzada()).toBeFalse();
      componente.toggleBusquedaAvanzada();
      expect(componente.mostrarBusquedaAvanzada()).toBeTrue();
      componente.toggleBusquedaAvanzada();
      expect(componente.mostrarBusquedaAvanzada()).toBeFalse();
    });
  });

  describe('Formulario de creación', () => {
    it('debe ser inválido cuando está vacío', () => {
      componente.abrirModalCrear();
      expect(componente.formularioCrear.invalid).toBeTrue();
    });

    it('debe ser válido con datos correctos', () => {
      componente.abrirModalCrear();
      componente.formularioCrear.patchValue({
        nombre: 'Test Usuario',
        telefono: '3219876543',
        correoElectronico: 'test@gmail.com'
      });
      expect(componente.formularioCrear.valid).toBeTrue();
    });

    it('no debe enviar si el formulario es inválido', () => {
      componente.abrirModalCrear();
      componente.onCrearCliente();
      expect(servicioMock.crearCliente).not.toHaveBeenCalled();
    });

    it('debe crear el cliente y recargar la lista si el formulario es válido', () => {
      servicioMock.crearCliente.and.returnValue(of(clientesMock[0]));
      servicioMock.obtenerTodos.and.returnValue(of(clientesMock));
      componente.abrirModalCrear();
      componente.formularioCrear.patchValue({
        nombre: 'Test Usuario',
        telefono: '3219876543',
        correoElectronico: 'test@gmail.com'
      });
      componente.onCrearCliente();
      expect(servicioMock.crearCliente).toHaveBeenCalled();
    });
  });

  describe('formatearFecha()', () => {
    it('debe retornar cadena vacía para fecha vacía', () => expect(componente.formatearFecha('')).toBe(''));
    it('debe retornar una fecha formateada', () => expect(componente.formatearFecha('2019-05-20')).toContain('20'));
  });
});
