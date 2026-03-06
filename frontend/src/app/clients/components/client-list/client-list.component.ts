import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule, FormGroup, Validators, NonNullableFormBuilder } from '@angular/forms';
import { Cliente, ClienteFilter, ClienteRequest } from '../../models/client.model';
import { ClienteService } from '../../services/client.service';

@Component({
  selector: 'app-lista-clientes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './client-list.component.html',
  styleUrl: './client-list.component.scss'
})
export class ClientListComponent implements OnInit {

  private readonly clienteService = inject(ClienteService);
  private readonly fb = inject(NonNullableFormBuilder);

  readonly clientes = signal<Cliente[]>([]);
  readonly cargando = signal(false);
  readonly mensajeError = signal('');
  readonly mensajeExito = signal('');
  readonly mostrarBusquedaAvanzada = signal(false);
  readonly mostrarModalCrear = signal(false);

  readonly totalClientes = computed(() => this.clientes().length);

  claveSimple = '';

  formularioBusqueda!: FormGroup;
  formularioCrear!: FormGroup;

  ngOnInit(): void {
    this.inicializarFormularios();
    this.cargarClientes();
  }

  private inicializarFormularios(): void {
    this.formularioBusqueda = this.fb.group({
      nombre: [''],
      telefono: [''],
      correoElectronico: [''],
      fechaDesde: [''],
      fechaHasta: ['']
    });

    this.formularioCrear = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      telefono: ['', [Validators.required, Validators.pattern(/^[0-9+\-\s()]{7,20}$/)]],
      correoElectronico: ['', [Validators.required, Validators.email]],
      fechaInicio: [''],
      fechaFin: ['']
    });
  }

  cargarClientes(): void {
    this.cargando.set(true);
    this.mensajeError.set('');
    this.clienteService.obtenerTodos().subscribe({
      next: data => { this.clientes.set(data); this.cargando.set(false); },
      error: err => {
        this.mensajeError.set('No se pudieron cargar los clientes. Intente nuevamente.');
        this.cargando.set(false);
        console.error(err);
      }
    });
  }

  onBusquedaSimple(): void {
    if (!this.claveSimple.trim()) { this.cargarClientes(); return; }
    this.cargando.set(true);
    this.clienteService.buscarPorClave(this.claveSimple).subscribe({
      next: data => { this.clientes.set(data); this.cargando.set(false); },
      error: err => { this.mensajeError.set('Error al realizar la búsqueda.'); this.cargando.set(false); console.error(err); }
    });
  }

  onBusquedaAvanzada(): void {
    const filtro: ClienteFilter = this.formularioBusqueda.value;
    this.cargando.set(true);
    this.clienteService.busquedaAvanzada(filtro).subscribe({
      next: data => {
        this.clientes.set(data);
        this.cargando.set(false);
        this.mostrarBusquedaAvanzada.set(false);
      },
      error: err => { this.mensajeError.set('Error en la búsqueda avanzada.'); this.cargando.set(false); console.error(err); }
    });
  }

  onCrearCliente(): void {
    if (this.formularioCrear.invalid) { this.formularioCrear.markAllAsTouched(); return; }
    const request: ClienteRequest = this.formularioCrear.value;
    this.clienteService.crearCliente(request).subscribe({
      next: cliente => {
        this.mensajeExito.set(`Cliente "${cliente.nombreCompleto}" creado exitosamente.`);
        this.mostrarModalCrear.set(false);
        this.formularioCrear.reset();
        this.cargarClientes();
        setTimeout(() => this.mensajeExito.set(''), 4000);
      },
      error: err => {
        const msg = err?.error?.mensaje ?? 'Error al crear el cliente.';
        this.mensajeError.set(msg);
        setTimeout(() => this.mensajeError.set(''), 4000);
      }
    });
  }

  toggleBusquedaAvanzada(): void {
    this.mostrarBusquedaAvanzada.update(v => !v);
  }

  abrirModalCrear(): void {
    this.mostrarModalCrear.set(true);
    this.formularioCrear.reset();
  }

  cerrarModalCrear(): void {
    this.mostrarModalCrear.set(false);
    this.formularioCrear.reset();
  }

  exportarCsv(): void {
    this.clienteService.exportarCsv();
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '';
    return new Date(fecha).toLocaleDateString('es-CO', {
      day: '2-digit', month: '2-digit', year: 'numeric'
    });
  }

  obtenerErrorCampo(form: FormGroup, campo: string): string {
    const ctrl = form.get(campo);
    if (!ctrl?.touched || !ctrl.errors) return '';
    if (ctrl.errors['required'])   return 'Este campo es obligatorio';
    if (ctrl.errors['email'])      return 'Ingrese un correo electrónico válido';
    if (ctrl.errors['minlength'])  return `Mínimo ${ctrl.errors['minlength'].requiredLength} caracteres`;
    if (ctrl.errors['maxlength'])  return `Máximo ${ctrl.errors['maxlength'].requiredLength} caracteres`;
    if (ctrl.errors['pattern'])    return 'El formato ingresado no es válido';
    return 'Valor inválido';
  }
}
