# Sistema de Gestión y Reserva de Citas para una Peluquería

Documentación técnica para desarrolladores · Curso Integrador I: Sistemas – Software · UTP · APF2

> Alternativa seleccionada: **aplicación web Java (Spring Boot + Thymeleaf) con MySQL** (≈65 % Java, cumple el mínimo de 50 %).

## 1. Alcance

Incluye: registro de clientes y estilistas; reserva, confirmación, reprogramación y cancelación de citas; catálogo de servicios con precio y duración; notificaciones y recordatorios automáticos; reportes de gestión.
Fuera de alcance (versión actual): inventario, facturación electrónica, planilla y campañas de marketing.

## 2. Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.x (Spring MVC, Spring Data JPA, Spring Security, Validation) |
| Vistas | Thymeleaf + HTML/CSS + JavaScript ligero |
| Base de datos | MySQL 8.0 (InnoDB, utf8mb4) |
| Construcción | Maven |
| Pruebas | JUnit 5, Mockito, Spring Boot Test, Spring Security Test (TDD) |
| Librerías de apoyo | Google Guava 33.3.1, Apache Commons Lang3, Apache POI 5.3.0, Logback |
| Documentación | Javadoc (`mvn javadoc:javadoc`), diagramas Mermaid en `docs/DIAGRAMAS.md` |

## 3. Arquitectura por capas

```
Navegador ⇄ Controller (Thymeleaf) → Service (@Transactional) → Repository (JPA) → MySQL
                                        └→ NotificacionService → WhatsApp Business API / SMTP
                     RecordatorioScheduler (@Scheduled) ─────────┘
```

Estructura de paquetes sugerida:

```
src/main/java/pe/utp/peluqueria
├── config/        SecurityConfig, LoginExitosoHandler, AdminInicial, DemoDataSeeder
├── controller/    AuthController, CitaController, CatalogoController, NotificacionController, AdminController, AgendaController, ReporteController
├── service/       CitaService, CatalogoService, UsuarioService, NotificacionService, ReporteService,
│                  ReservaPendienteService (interfaz) + ReservaPendienteSesionService, ExcelExportService
├── repository/    UsuarioRepository, CitaRepository, ServicioRepository, NotificacionRepository ...   (capa DAO: Spring Data JPA)
├── model/         Usuario, Cliente, Estilista, Cita, DetalleCita, Servicio, Notificacion ...
├── dto/           ReservaDto, ReservaPendiente, ServicioDto, RegistroDto
├── util/          Acceso, Privacidad
├── scheduler/     RecordatorioScheduler
└── exception/     CruceHorarioException, RecursoNoEncontradoException
src/main/resources
├── logback-spring.xml
├── templates/     login, registro, servicios, reservar, notificaciones, mis-citas, admin/*, reportes/*
├── static/        css, js, img
└── application.properties
```

## 4. Configuración y ejecución

1. Crear la base de datos ejecutando `database/schema.sql` en MySQL 8.
2. Definir las credenciales por variables de entorno (nunca en el repositorio):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/peluqueria_citas?useSSL=true&serverTimezone=America/Lima
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
```

3. Ejecutar: `mvn spring-boot:run` y abrir `http://localhost:8080`.

## 5. Modelo de datos

13 tablas en 3FN: `rol`, `sede`, `usuario`, `cliente`, `estilista`, `horario_estilista`, `categoria_servicio`, `servicio`, `estilista_servicio`, `cita`, `detalle_cita`, `notificacion`, `auditoria`. Ver `schema.sql` y el informe (sección de diseño de base de datos).

Reglas clave:
- `cita` tiene `UNIQUE (id_estilista, fecha, hora_inicio_activa)`. La columna generada es `NULL` si la cita está cancelada, por lo que el horario vuelve a quedar libre.
- `detalle_cita` guarda `precio_aplicado` y `duracion_min` vigentes al reservar (un cambio de precio no altera citas pasadas).
- No se borran citas físicamente: se cambian de estado.

## 6. Reglas de negocio

| ID | Regla |
|---|---|
| RN01 | Una cita no puede cruzarse con otra activa del mismo estilista (se valida el rango `hora_inicio–hora_fin`). |
| RN02 | La duración de la cita es la suma de las duraciones de sus servicios. |
| RN03 | Solo se ofrecen horarios dentro de `horario_estilista` y que estén libres. |
| RN04 | Estados: `PENDIENTE → CONFIRMADA → ATENDIDA`; también `CANCELADA` y `AUSENTE`. Una cita `ATENDIDA` o `CANCELADA` no cambia de estado. |
| RN05 | Toda creación, confirmación, reprogramación o cancelación genera una notificación. |
| RN06 | Los recordatorios se programan 24 h y 2 h antes de la cita si el cliente los aceptó. |
| RN07 | Los servicios inactivos no se muestran en el catálogo. |

Para evitar reservas simultáneas sobre el mismo horario, `CitaService.crearCita` es `@Transactional` y consulta el cruce con bloqueo (`@Lock(PESSIMISTIC_WRITE)`); la restricción `UNIQUE` es la última defensa.

## 7. Rutas principales (Spring MVC)

| Método | Ruta | Descripción | Rol |
|---|---|---|---|
| GET | `/` | Arranque: invitado → `/servicios`; con sesión → `/inicio` (enruta por rol) | Público |
| GET/POST | `/login`, `/registro` | Autenticación y alta de cliente | Público |
| GET | `/servicios` | Catálogo de servicios | Invitado / Cliente |
| GET | `/reservar` | Asistente de reserva (4 pasos); `?retomar` restaura la selección guardada | Invitado / Cliente |
| GET | `/reservar/horarios?estilista=&fecha=&servicios=` | Horarios libres (JSON) | Invitado / Cliente |
| POST | `/reservar/pendiente` | «Confirmar cita» sin sesión: guarda la selección y envía a `/login?reservar` | Invitado |
| POST | `/reservar` | Registrar cita | Cliente |
| GET | `/notificaciones` | Avisos WhatsApp/correo; al invitado le pide iniciar sesión | Invitado / Cliente |
| GET | `/mis-citas` | Próximas e historial | Cliente |
| POST | `/citas/{id}/cancelar`, `/citas/{id}/reprogramar` | Cambios de cita | Cliente / Admin |
| GET | `/admin/citas` | Gestión de citas | Admin |
| POST | `/admin/citas/{id}/confirmar` | Confirmar cita | Admin / Estilista |
| GET/POST | `/admin/servicios`, `/admin/estilistas` | Mantenimientos | Admin |
| GET | `/agenda` | Agenda del estilista | Estilista |
| POST | `/agenda/{id}/atendida` | Marcar atendida o ausente | Estilista |
| GET | `/admin/reportes/citas`, `/admin/reportes/servicios` | Reportes | Admin |
| GET | `/admin/reportes/excel?desde=&hasta=` | Exporta el reporte a .xlsx (Apache POI) | Admin |

## 8. Seguridad

- Contraseñas con **BCrypt**; nunca se registran ni se devuelven.
- **Spring Security** con roles `CLIENTE`, `ESTILISTA`, `ADMINISTRADOR`; rutas `/admin/**` solo para administrador.
- Protección **CSRF** activa en formularios; validación de entradas con Bean Validation.
- Acceso a datos con JPA (consultas parametrizadas) para prevenir inyección SQL.
- Un cliente solo puede ver y modificar **sus** citas (se valida el `id` contra el usuario autenticado).
- Usuario de BD con mínimo privilegio y usuario de solo lectura para reportes.
- Tabla `auditoria` para acciones sensibles; HTTPS en producción; respaldos diarios.
- **Logback** (`logback-spring.xml`): sin contraseñas ni valores SQL en logs, correos enmascarados con `Privacidad.enmascararEmail`, archivo rotativo (10 MB / 14 días).
- **Apache Commons Lang3**: los textos libres (motivo de cancelación) se limpian y limitan a 200 caracteres.
- **Apache POI**: el Excel escribe siempre texto (nunca fórmulas) para evitar inyección de fórmulas.
- Sesión: cookie `HttpOnly`, `SameSite=Lax`, vigencia 30 min, cambio de ID de sesión al autenticar (anti *session fixation*).

### 8.1 Acceso de invitado y reserva pendiente

La aplicación **arranca como invitado** en el catálogo. El invitado puede explorar servicios y recorrer el asistente de reserva, pero **no se crea ninguna cita** hasta que un cliente autenticado confirma.

1. En el paso 4 el formulario de un invitado apunta a `POST /reservar/pendiente` (CSRF activo). Si los datos son válidos se guardan en la **sesión HTTP** como `ReservaPendiente` (inmutable, Guava) y se redirige a `/login?reservar`, que muestra «primero debes iniciar sesión».
2. `LoginExitosoHandler` decide el destino: **CLIENTE** con reserva vigente → `/reservar?retomar` (paso 4 restaurado); **ADMINISTRADOR / ESTILISTA** → `/inicio` (su ventana habitual) y la reserva se **descarta**.
3. La reserva caduca a los 30 min (`app.reserva-pendiente.minutos`), se borra al confirmar y al cerrar sesión (la sesión se invalida).
4. Al confirmar, `CitaService.crearCita` vuelve a validar todo (RN01–RN03); la sesión nunca es fuente de confianza.
5. Administrador y estilista autenticados que abran `/servicios`, `/reservar` o `/notificaciones` son redirigidos a su propia ventana.

## 9. Convenciones

- Clases en `PascalCase`, métodos y atributos en `camelCase`, tablas y columnas en `snake_case`.
- Commits con prefijo `feat:`, `fix:`, `docs:`; una rama por historia de usuario.
- Cobertura mínima sugerida: 70 % en la capa de servicios.

## 10. Requisitos no funcionales a verificar

Reservar en máximo 3 pasos · catálogo en menos de 2 s · disponibilidad objetivo 95 % · compatibilidad con Chrome, Firefox y Edge · contraseñas cifradas · arquitectura por capas.
