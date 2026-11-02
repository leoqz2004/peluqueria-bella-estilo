package pe.utp.peluqueria.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import pe.utp.peluqueria.dto.ReservaDto;

/** TDD del acceso de invitado: la seleccion se guarda, se recupera, caduca y se descarta. */
class ReservaPendienteSesionServiceTest {
    private static final Clock BASE = Clock.fixed(Instant.parse("2026-11-01T15:00:00Z"), ZoneOffset.UTC);

    private ReservaDto dto() {
        ReservaDto d = new ReservaDto();
        d.setEstilistaId(10L); d.setServicioIds(List.of(1L, 2L));
        d.setFecha(LocalDate.of(2026, 11, 5)); d.setHora(LocalTime.of(10, 30));
        return d;
    }

    @Test
    void guardaYRecuperaLaSeleccion() {
        var servicio = new ReservaPendienteSesionService(30, BASE);
        MockHttpSession sesion = new MockHttpSession();
        servicio.guardar(sesion, dto());

        var pendiente = servicio.obtenerVigente(sesion).orElseThrow();
        assertEquals(10L, pendiente.getEstilistaId());
        assertEquals(List.of(1L, 2L), pendiente.getServicioIds());
        assertEquals("1,2", pendiente.getServiciosCsv());
        assertEquals(LocalTime.of(10, 30), pendiente.getHora());
    }

    @Test
    void sinSeleccionDevuelveVacio() {
        var servicio = new ReservaPendienteSesionService(30, BASE);
        assertTrue(servicio.obtenerVigente(new MockHttpSession()).isEmpty());
        assertTrue(servicio.obtenerVigente(null).isEmpty());
    }

    @Test
    void caducaDespuesDeLaVigenciaYSeElimina() {
        MockHttpSession sesion = new MockHttpSession();
        new ReservaPendienteSesionService(30, BASE).guardar(sesion, dto());

        var pasados31 = new ReservaPendienteSesionService(30, Clock.offset(BASE, Duration.ofMinutes(31)));
        assertTrue(pasados31.obtenerVigente(sesion).isEmpty());
        assertNull(sesion.getAttribute(ReservaPendienteSesionService.ATRIBUTO), "debe borrarse al caducar");
    }

    @Test
    void sigueVigenteDentroDelPlazo() {
        MockHttpSession sesion = new MockHttpSession();
        new ReservaPendienteSesionService(30, BASE).guardar(sesion, dto());
        var pasados29 = new ReservaPendienteSesionService(30, Clock.offset(BASE, Duration.ofMinutes(29)));
        assertTrue(pasados29.obtenerVigente(sesion).isPresent());
    }

    @Test
    void descartarEliminaLaSeleccion() {
        var servicio = new ReservaPendienteSesionService(30, BASE);
        MockHttpSession sesion = new MockHttpSession();
        servicio.guardar(sesion, dto());
        servicio.descartar(sesion);
        assertTrue(servicio.obtenerVigente(sesion).isEmpty());
        assertDoesNotThrow(() -> servicio.descartar(null));
    }

    @Test
    void guardarReemplazaLaSeleccionAnterior() {
        var servicio = new ReservaPendienteSesionService(30, BASE);
        MockHttpSession sesion = new MockHttpSession();
        servicio.guardar(sesion, dto());
        ReservaDto otra = dto(); otra.setEstilistaId(99L);
        servicio.guardar(sesion, otra);
        assertEquals(99L, servicio.obtenerVigente(sesion).orElseThrow().getEstilistaId());
    }

    @Test
    void rechazaSeleccionIncompleta() {
        ReservaDto incompleta = dto(); incompleta.setServicioIds(List.of());
        assertThrows(IllegalArgumentException.class,
            () -> new ReservaPendienteSesionService(30, BASE).guardar(new MockHttpSession(), incompleta));
    }
}
