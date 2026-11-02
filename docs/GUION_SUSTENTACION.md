# Guion de sustentación oral · Avance de Proyecto Final 3 (≈ 10 min)

Todos los integrantes deben hablar. Se propone repartir por secciones (ajusten nombres y orden). Cada sección se vincula con un concepto del curso, que es lo que evalúa la rúbrica.

| Min | Sección | Quién | Qué decir | Concepto del curso a nombrar |
|---|---|---|---|---|
| 0:00–1:00 | **Analiza el contexto** | Integrante 1 | Problema: reservas por llamada/WhatsApp, cruces de horarios, olvidos. Objetivo: reservar en pocos pasos y con recordatorios | Requisitos, reglas de negocio RN01–RN06 |
| 1:00–2:00 | **Analiza alternativas** | Integrante 1 | Web Java (Spring Boot) vs. escritorio vs. móvil; por qué web | Comparación de alternativas |
| 2:00–4:00 | **Diseña la solución** | Integrante 2 | Mostrar `docs/DIAGRAMAS.md`: capas MVC + DAO; SOLID (interfaz `ReservaPendienteService`); seguridad (BCrypt, CSRF, roles) | **MVC, DAO, SOLID, seguridad** |
| 4:00–5:00 | **Librerías** | Integrante 3 | Guava, Commons, POI y Logback: *dónde* se usa cada una y *por qué es segura* (sin contraseñas en logs, Excel sin fórmulas) | Librerías de apoyo |
| 5:00–7:30 | **Diseña prototipo / Construye el producto (demo)** | Integrante 4 | Demo en vivo (ver abajo) | UX/UI, flujo de sesión |
| 7:30–8:30 | **TDD** | Integrante 2 o 3 | Ejecutar `mvn test`; explicar 2 pruebas: «la reserva se conserva solo para cliente» y «el invitado no ve notificaciones» | **TDD** |
| 8:30–9:30 | **Control de versiones** | Integrante 5 / responsable Git | Mostrar GitHub: ramas, commits por integrante, Pull Request | Git y GitHub |
| 9:30–10:00 | **Cierre** | Todos | Alcance logrado, pendientes (PDF, edición de horarios) y próximos pasos | — |

## Demo en vivo (2 min 30 s)
1. `mvn spring-boot:run` y abrir `http://localhost:8080` → **entra al catálogo sin login** (invitado).
2. Abrir **Notificaciones** → pide iniciar sesión (no hay avisos de WhatsApp/correo).
3. Elegir un servicio → estilista → fecha y hora → **«Confirmar cita»** → aparece «primero debes iniciar sesión» y va al login.
4. Entrar como cliente (`valeria.rojas@correo.com` / `Cliente123!`) → **vuelve al paso 4 con todo elegido** → confirmar → *Mis citas*.
5. Cerrar sesión (vuelve al catálogo). Repetir los pasos 3–4 pero entrando como **estilista** (`milagros@bellaestilo.pe` / `Estilista123!`) → va a su agenda y no conserva la reserva.
6. Como recepción (`recepcion@bellaestilo.pe` / `Admin123!`): Reportes → **Exportar Excel**.

## Preguntas que pueden hacer (preparen respuesta)
- *¿Por qué guardan la reserva en la sesión y no en la base de datos?* Porque aún no hay cliente: crear una cita sin dueño rompería la integridad. La cita real solo se crea al confirmar, con las reglas RN01–RN03.
- *¿Qué pasa si alguien ocupa ese horario mientras inicia sesión?* Al confirmar se vuelve a validar el cruce (bloqueo pesimista) y se informa el conflicto.
- *¿Dónde está DAO?* En `repository/`: Spring Data JPA es el único acceso a datos.
- *¿Qué principio SOLID mostraron?* Inversión de dependencias: los controladores usan la interfaz `ReservaPendienteService`.
- *¿Cómo evitan filtrar datos sensibles en logs?* `logback-spring.xml` desactiva SQL con parámetros y `Privacidad.enmascararEmail`.
- *¿Por qué el Excel es seguro?* Todo se escribe como texto, nunca como fórmula (hay una prueba que lo verifica).

## Consejos para la fluidez (criterio de la rúbrica)
Ensayen con cronómetro, cada uno conecta su parte con la siguiente («con este diseño, ahora X les muestra…»), eviten leer, y nombren siempre el concepto del curso junto a lo que muestran.
