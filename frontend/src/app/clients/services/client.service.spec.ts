import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { ClienteService } from './client.service';
import { Cliente, ClienteFilter, ClienteRequest } from '../models/client.model';

describe('ClienteService', () => {
  let servicio: ClienteService;
  let httpMock: HttpTestingController;

  const clientesMock: Cliente[] = [
    { claveCompartida: 'jgutierrez', nombreCompleto: 'Juliana Gutierrez', correoElectronico: 'j@gmail.com', telefono: '3219876543', fechaRegistro: '2019-05-20' },
    { claveCompartida: 'mmartinez',  nombreCompleto: 'Manuel Martinez',   correoElectronico: 'm@gmail.com', telefono: '3219876543', fechaRegistro: '2019-05-20' }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ClienteService,
        provideHttpClient(withFetch()),
        provideHttpClientTesting()
      ]
    });
    servicio  = TestBed.inject(ClienteService);
    httpMock  = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('debe crearse correctamente', () => {
    expect(servicio).toBeTruthy();
  });

  describe('obtenerTodos()', () => {
    it('debe realizar GET /api/clientes y retornar la lista', () => {
      servicio.obtenerTodos().subscribe(clientes => {
        expect(clientes.length).toBe(2);
        expect(clientes[0].claveCompartida).toBe('jgutierrez');
      });
      const req = httpMock.expectOne('/api/clientes');
      expect(req.request.method).toBe('GET');
      req.flush(clientesMock);
    });
  });

  describe('buscarPorClave()', () => {
    it('debe realizar GET /api/clientes con el parámetro clave', () => {
      servicio.buscarPorClave('jgut').subscribe(c => expect(c.length).toBe(1));
      const req = httpMock.expectOne(r =>
        r.url === '/api/clientes' && r.params.get('clave') === 'jgut');
      expect(req.request.method).toBe('GET');
      req.flush([clientesMock[0]]);
    });
  });

  describe('crearCliente()', () => {
    it('debe realizar POST /api/clientes con los datos del cliente', () => {
      const request: ClienteRequest = { nombre: 'Juliana Gutierrez', telefono: '3219876543', correoElectronico: 'j@gmail.com' };
      servicio.crearCliente(request).subscribe(c => expect(c.claveCompartida).toBe('jgutierrez'));
      const req = httpMock.expectOne('/api/clientes');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(clientesMock[0]);
    });
  });

  describe('busquedaAvanzada()', () => {
    it('debe realizar POST /api/clientes/busqueda con el filtro', () => {
      const filtro: ClienteFilter = { nombre: 'Juliana' };
      servicio.busquedaAvanzada(filtro).subscribe(c => expect(c).toEqual([clientesMock[0]]));
      const req = httpMock.expectOne('/api/clientes/busqueda');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(filtro);
      req.flush([clientesMock[0]]);
    });
  });
});
