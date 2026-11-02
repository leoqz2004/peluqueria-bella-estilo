# Diagramas de arquitectura · Bella Estilo

> Los diagramas están en Mermaid: se visualizan directamente en GitHub y en VS Code (extensión *Markdown Preview Mermaid Support*).

## 1. Arquitectura por capas (MVC + DAO)

```mermaid
flowchart LR
    N[Navegador] -->|HTTP| C
    subgraph MVC
      C[Controller<br/>AuthController · CitaController · CatalogoController<br/>NotificacionController · ReporteController]
      V[View<br/>Thymeleaf + CSS/JS]
      M[Model<br/>Cita · Cliente · Servicio · Usuario ...]
    end
    C --> V
    C --> S
    subgraph Service
      S[Services @Transactional<br/>CitaService · UsuarioService · NotificacionService<br/>ReservaPendienteService · ExcelExportService]
    end
    S --> R
    subgraph DAO
      R[Repository - Spring Data JPA<br/>CitaRepository · ClienteRepository ...]
    end
    R --> DB[(H2 demo / MySQL)]
    S -.usa.-> M
    SEC[Spring Security<br/>SecurityConfig · LoginExitosoHandler] -.filtra.-> C
    LOG[Logback] -.registra.-> S
```

## 2. Flujo de acceso: invitado → login → reserva

```mermaid
sequenceDiagram
    actor U as Usuario
    participant C as CitaController
    participant P as ReservaPendienteService
    participant L as LoginExitosoHandler
    participant S as CitaService
    U->>C: GET / (sin sesión)
    C-->>U: redirect /servicios (catálogo de invitado)
    U->>C: GET /reservar → elige servicio, estilista, fecha, hora
    U->>C: POST /reservar/pendiente («Confirmar cita»)
    C->>P: guardar(sesión, selección)
    C-->>U: redirect /login?reservar ("primero debes iniciar sesión")
    U->>L: POST /login (credenciales)
    alt rol CLIENTE y reserva vigente
        L-->>U: redirect /reservar?retomar (paso 4 restaurado)
        U->>C: POST /reservar (Confirmar)
        C->>S: crearCita (RN01–RN03)
        C->>P: descartar(sesión)
        C-->>U: redirect /mis-citas?creada
    else rol ADMINISTRADOR o ESTILISTA
        L->>P: descartar(sesión)
        L-->>U: redirect /inicio → su ventana habitual
    end
```

## 3. Decisión de acceso por ruta

```mermaid
flowchart TD
    A[Petición] --> B{¿Ruta pública de invitado?<br/>GET / /servicios /reservar<br/>/reservar/horarios /notificaciones}
    B -- sí --> C{¿Autenticado con rol<br/>distinto de CLIENTE?}
    C -- sí --> D[redirect /inicio<br/>su ventana de rol]
    C -- no --> E[Mostrar vista de cliente / invitado]
    B -- no --> F{¿Autenticado y con rol permitido?}
    F -- sí --> G[Atender]
    F -- no --> H[redirect /login]
```

## 4. Principios SOLID y patrones aplicados

| Principio / patrón | Dónde se aplica |
|---|---|
| **S** – Responsabilidad única | `LoginExitosoHandler` (solo decide destino), `Acceso` (solo consulta de rol), `Privacidad` (solo enmascarar), `ExcelExportService` (solo generar Excel) |
| **O** – Abierto/cerrado | Nuevas reglas de redirección se agregan en el handler sin modificar `SecurityConfig` ni los controladores |
| **L** – Sustitución de Liskov | `ReservaPendienteSesionService` puede sustituirse por otra implementación (p. ej. en base de datos) sin cambiar a quien la usa |
| **I** – Segregación de interfaces | `ReservaPendienteService` expone solo 3 operaciones (`guardar`, `obtenerVigente`, `descartar`) |
| **D** – Inversión de dependencias | Controladores y handler dependen de la interfaz `ReservaPendienteService`, no de la clase concreta |
| **MVC** | Controller (`controller/`) · View (`templates/`) · Model (`model/`, `dto/`) |
| **DAO** | `repository/` (Spring Data JPA): único punto de acceso a la base de datos |
| **TDD** | Pruebas unitarias e integración en `src/test` para cada regla del flujo de invitado |
