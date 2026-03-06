export interface Cliente {
  claveCompartida: string;
  nombreCompleto: string;
  correoElectronico: string;
  telefono: string;
  fechaRegistro: string;
  fechaInicio?: string;
  fechaFin?: string;
}

export interface ClienteRequest {
  nombre: string;
  telefono: string;
  correoElectronico: string;
  fechaInicio?: string;
  fechaFin?: string;
}

export interface ClienteFilter {
  nombre?: string;
  telefono?: string;
  correoElectronico?: string;
  fechaDesde?: string;
  fechaHasta?: string;
}
