# Bella Estilo · Gestión y Reserva de Citas (APF2)

Aplicación web **Java 17 + Spring Boot 3** (Spring MVC, Thymeleaf, Spring Data JPA, Spring Security) basada en el informe APF2.
Diseño: pantallas P01–P16 del prototipo (paleta vinotinto, tarjetas, tabs, modales, asistente de 4 pasos).

## Ejecutar la demo (sin instalar MySQL)
Requisitos: JDK 17 y Maven 3.9+.

```bash
mvn spring-boot:run
# abrir http://localhost:8080
```
El perfil por defecto es `demo`: base **H2 en memoria** con datos de ejemplo (se reinicia en cada arranque).

| Rol | Usuario | Clave |
|---|---|---|
| Cliente | `valeria.rojas@correo.com` | `Cliente123!` |
| Recepción (Administrador) | `recepcion@bellaestilo.pe` | `Admin123!` |
| Estilista | `milagros@bellaestilo.pe` (también `rosa@…`, `jimena@…`) | `Estilista123!` |

## Ejecutar con MySQL
1. Ejecutar `database/schema.sql` en MySQL 8 (cambiar las claves de los usuarios de BD).
2. Variables de entorno: `SPRING_PROFILES_ACTIVE=mysql`, `DB_USER`, `DB_PASSWORD`, y opcionalmente `ADMIN_EMAIL`, `ADMIN_PASSWORD` para crear el primer administrador.
3. `mvn spring-boot:run`

## Qué está implementado (≈ 50 % del alcance)
| Pantalla | Estado |
|---|---|
| P01 Login · P02 Registro | ✅ (recuperar contraseña RF03: pendiente) |
| P03 Catálogo (cards, filtro y búsqueda) | ✅ |
| P04–P06 Reserva en 4 pasos con horarios libres y sin cruces | ✅ |
| P07 Mis citas (tabs, modal cancelar, reprogramar) | ✅ |
| P08 Notificaciones y preferencias | ✅ (envío real WhatsApp/correo simulado en log) |
| P09 Panel de recepción · P10 Gestión de citas (modales) | ✅ |
| P11 Servicios (crear, editar, activar/desactivar) | ✅ |
| P12 Estilistas y horarios | ◐ alta y consulta; edición de horario pendiente |
| P13 Clientes | ◐ listado y búsqueda; "ver historial" pendiente |
| P14–P15 Reportes | ◐ en pantalla; exportar PDF/Excel y % de ocupación pendientes |
| P16 Agenda del estilista | ✅ |

Reglas de negocio RN01–RN06: validación de cruce con transacción y bloqueo pesimista, duración = suma de servicios, horarios dentro de la jornada, estados y transiciones, notificaciones y recordatorios 24 h / 2 h, sin borrado físico.

## Pruebas
`mvn test` (JUnit 5 + Mockito) sobre `CitaService`.

## Estructura
`controller/` · `service/` (reglas de negocio) · `repository/` · `model/` · `dto/` · `scheduler/` · `config/` · `templates/` (Thymeleaf) · `static/` (CSS/JS).
