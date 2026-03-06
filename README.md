# Prueba Técnica — Alianza Fiduciaria y Valores

Aplicación fullstack para gestión de clientes. Desarrollada como prueba técnica para el cargo de Ingeniero Fullstack.

---

## Tecnologías usadas

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Backend | Java + Spring Boot | 21 / 4.0.3 |
| Frontend | Angular | 21.2.1 |
| Tests backend | JUnit 5 + Mockito | 5.x |
| Tests frontend | Jasmine + Karma | 5.4 / 6.4 |
| Build backend | Maven | 3.9+ |
| Estilos | SCSS | — |

---

## Estructura del proyecto

```
alianza-solution/
├── backend/        → API REST en Spring Boot
└── frontend/       → SPA en Angular
```

```
backend/
└── com.alianza.clients/
    ├── controller/     → Endpoints REST
    ├── service/        → Lógica de negocio
    ├── repository/     → Almacenamiento en memoria
    ├── model/          → Entidades y DTOs
    └── exception/      → Manejo global de errores
```

```
frontend/
└── src/app/clients/
    ├── components/     → Vistas y lógica de UI
    ├── services/       → Llamadas al backend
    └── models/         → Interfaces TypeScript
```

---

## Cómo correr el proyecto

> Requisitos: Java 21+, Maven 3.9+, Node.js 22+

**1. Backend** — abrir una terminal:
```bash
cd backend
mvn spring-boot:run
```
Corre en → http://localhost:8080

**2. Frontend** — abrir otra terminal:
```bash
cd frontend
npm install --legacy-peer-deps
npm start
```
Corre en → http://localhost:4200

> El frontend se comunica con el backend automáticamente a través del proxy configurado en `proxy.conf.json`.

---

## Endpoints disponibles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/clientes` | Listar todos los clientes |
| GET | `/api/clientes?clave=xxx` | Búsqueda simple por clave |
| GET | `/api/clientes/{clave}` | Obtener cliente por clave exacta |
| POST | `/api/clientes` | Crear nuevo cliente |
| POST | `/api/clientes/busqueda` | Búsqueda avanzada con filtros |
| GET | `/api/clientes/exportar/csv` | Exportar lista a CSV |

### Ejemplo — crear cliente
```bash
curl -X POST http://localhost:8080/api/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Maria Lopez",
    "telefono": "3001234567",
    "correoElectronico": "mlopez@gmail.com",
    "fechaInicio": "2024-01-01",
    "fechaFin": "2024-12-31"
  }'
```

Respuesta:
```json
{
  "claveCompartida": "mlopez",
  "nombreCompleto": "Maria Lopez",
  "correoElectronico": "mlopez@gmail.com",
  "telefono": "3001234567",
  "fechaInicio": "2024-01-01",
  "fechaFin": "2024-12-31",
  "fechaRegistro": "2026-03-06"
}
```

---

## Funcionalidades implementadas

### Obligatorias ✅
- Listado de clientes con tabla
- Búsqueda simple por clave compartida
- Formulario de creación con validaciones (frontend y backend)
- Almacenamiento en memoria con datos de ejemplo precargados
- API REST con Spring Boot
- Diseño responsive con sidebar de navegación
- Sistema de logs con archivo rotativo

### Opcionales ✅
- Búsqueda avanzada por nombre, teléfono, correo y rango de fechas
- Exportación a CSV

---

## Pruebas unitarias

**Backend:**
```bash
cd backend
mvn test
```

**Frontend:**
```bash
cd frontend
npm test
```

### Qué se prueba

**Backend (JUnit 5 + Mockito):**
- Obtener todos los clientes
- Búsqueda simple y avanzada
- Creación con clave duplicada
- Cliente no encontrado
- Exportación CSV

**Frontend (Jasmine):**
- Carga inicial de clientes
- Señales reactivas (Signals)
- Validaciones del formulario
- Llamadas HTTP al servicio

---

## Generación de clave compartida

La clave se genera automáticamente del nombre:

| Nombre | Clave generada |
|--------|---------------|
| Juliana Gutierrez | `jgutierrez` |
| Carlos Ariza | `cariza` |
| Ana Ruiz | `aruiz` |

Formato: primera letra del primer nombre + apellido, todo en minúsculas.

---

## Características técnicas destacadas

- **Angular 21**: Signals, `inject()`, control flow (`@if`, `@for`) sin NgModules
- **Spring Boot 4**: Jakarta EE 11, `@MockitoBean` en tests, inferencia con `var`
- **Sin base de datos**: almacenamiento en `ConcurrentHashMap` (thread-safe)
- **Logs**: nivel DEBUG para el paquete principal, archivo rotativo de 10MB / 30 días
