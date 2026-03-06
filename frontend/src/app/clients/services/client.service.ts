import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Cliente, ClienteRequest, ClienteFilter } from '../models/client.model';

@Injectable({ providedIn: 'root' })
export class ClienteService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/clientes';

  obtenerTodos(): Observable<Cliente[]> {
    console.log('[ClienteService] Consultando todos los clientes');
    return this.http.get<Cliente[]>(this.apiUrl).pipe(
      tap(clientes => console.log(`[ClienteService] ${clientes.length} clientes cargados`)),
      catchError(err => {
        console.error('[ClienteService] Error al obtener clientes', err);
        return throwError(() => err);
      })
    );
  }

  buscarPorClave(clave: string): Observable<Cliente[]> {
    console.log(`[ClienteService] Búsqueda por clave: '${clave}'`);
    const params = new HttpParams().set('clave', clave);
    return this.http.get<Cliente[]>(this.apiUrl, { params }).pipe(
      tap(r => console.log(`[ClienteService] ${r.length} resultado(s) encontrado(s)`)),
      catchError(err => {
        console.error('[ClienteService] Error en búsqueda por clave', err);
        return throwError(() => err);
      })
    );
  }

  busquedaAvanzada(filtro: ClienteFilter): Observable<Cliente[]> {
    console.log('[ClienteService] Búsqueda avanzada', filtro);
    return this.http.post<Cliente[]>(`${this.apiUrl}/busqueda`, filtro).pipe(
      tap(r => console.log(`[ClienteService] ${r.length} resultado(s) en búsqueda avanzada`)),
      catchError(err => {
        console.error('[ClienteService] Error en búsqueda avanzada', err);
        return throwError(() => err);
      })
    );
  }

  crearCliente(request: ClienteRequest): Observable<Cliente> {
    console.log('[ClienteService] Creando cliente', request);
    return this.http.post<Cliente>(this.apiUrl, request).pipe(
      tap(c => console.log(`[ClienteService] Cliente creado con clave: ${c.claveCompartida}`)),
      catchError(err => {
        console.error('[ClienteService] Error al crear cliente', err);
        return throwError(() => err);
      })
    );
  }

  exportarCsv(): void {
    console.log('[ClienteService] Exportando clientes a CSV');
    window.open(`${this.apiUrl}/exportar/csv`, '_blank');
  }
}
