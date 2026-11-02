package pe.utp.peluqueria.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import pe.utp.peluqueria.dto.ReservaDto;
import pe.utp.peluqueria.exception.*;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.*;

class CitaServiceTest {
    CitaRepository citas = mock(CitaRepository.class);
    ClienteRepository clientes = mock(ClienteRepository.class);
    EstilistaRepository estilistas = mock(EstilistaRepository.class);
    ServicioRepository servicios = mock(ServicioRepository.class);
    HorarioEstilistaRepository horarios = mock(HorarioEstilistaRepository.class);
    NotificacionService notif = mock(NotificacionService.class);
    CitaService service = new CitaService(citas, clientes, estilistas, servicios, horarios, notif, 30);

    Estilista estilista; Servicio corte; LocalDate lunes;

    static void setId(Object o, Long id) throws Exception {
        Class<?> c = o.getClass();
        while (c != Object.class) {
            try { Field f = c.getDeclaredField("id"); f.setAccessible(true); f.set(o, id); return; }
            catch (NoSuchFieldException e) { c = c.getSuperclass(); }
        }
    }

    @BeforeEach
    void preparar() throws Exception {
        corte = new Servicio(); setId(corte, 1L); corte.setNombre("Corte"); corte.setPrecio(new BigDecimal("35")); corte.setDuracionMin(40);
        estilista = new Estilista(); setId(estilista, 10L); estilista.getServicios().add(corte);
        estilista.setNombres("Ana"); estilista.setApellidos("Diaz"); estilista.setSede(new Sede());
        lunes = LocalDate.now().plusDays(1);
        while (lunes.getDayOfWeek() != DayOfWeek.MONDAY) lunes = lunes.plusDays(1);
        Cliente cliente = new Cliente(); setId(cliente, 5L);
        when(clientes.findById(5L)).thenReturn(Optional.of(cliente));
        when(estilistas.findById(10L)).thenReturn(Optional.of(estilista));
        when(servicios.findAllById(any())).thenReturn(List.of(corte));
        when(horarios.findByEstilistaIdAndDiaSemanaOrderByHoraInicio(eq(10L), anyInt()))
            .thenReturn(List.of(new HorarioEstilista(estilista, 1, LocalTime.of(9, 0), LocalTime.of(18, 0))));
    }

    ReservaDto reserva(LocalTime hora) {
        ReservaDto d = new ReservaDto();
        d.setEstilistaId(10L); d.setServicioIds(List.of(1L)); d.setFecha(lunes); d.setHora(hora);
        return d;
    }

    @Test
    void crearCita_calculaDuracionYTotal() {
        when(citas.buscarCrucesConBloqueo(any(), any(), any(), any(), any())).thenReturn(List.of());
        Cita c = service.crearCita(5L, reserva(LocalTime.of(10, 0)));
        assertEquals(LocalTime.of(10, 40), c.getHoraFin());              // RN02
        assertEquals(new BigDecimal("35"), c.getTotal());
        verify(notif).registrar(c, TipoNotificacion.REGISTRO);            // RN05
    }

    @Test
    void crearCita_conCruce_lanzaExcepcion() {                            // RN01
        when(citas.buscarCrucesConBloqueo(any(), any(), any(), any(), any())).thenReturn(List.of(new Cita()));
        assertThrows(CruceHorarioException.class, () -> service.crearCita(5L, reserva(LocalTime.of(10, 0))));
        verify(citas, never()).save(any());
    }

    @Test
    void crearCita_fueraDeJornada_lanzaExcepcion() {                      // RN03
        when(citas.buscarCrucesConBloqueo(any(), any(), any(), any(), any())).thenReturn(List.of());
        assertThrows(ReglaNegocioException.class, () -> service.crearCita(5L, reserva(LocalTime.of(17, 45))));
    }

    @Test
    void horariosLibres_excluyeRangoOcupado() {
        Cita ocupada = new Cita(); ocupada.setHoraInicio(LocalTime.of(9, 0)); ocupada.setHoraFin(LocalTime.of(10, 0));
        when(citas.activasDelDia(10L, lunes)).thenReturn(List.of(ocupada));
        List<LocalTime> libres = service.horariosLibres(10L, lunes, List.of(1L));
        assertFalse(libres.contains(LocalTime.of(9, 0)));
        assertFalse(libres.contains(LocalTime.of(9, 30)));
        assertTrue(libres.contains(LocalTime.of(10, 0)));
    }

    @Test
    void cancelarCitaAtendida_noCambiaEstado() throws Exception {         // RN04
        Cita c = new Cita(); setId(c, 1L); c.setEstado(EstadoCita.ATENDIDA);
        Usuario admin = new Usuario(); Rol r = new Rol(); r.setNombre("ADMINISTRADOR"); admin.setRol(r);
        when(citas.findById(1L)).thenReturn(Optional.of(c));
        assertThrows(ReglaNegocioException.class, () -> service.cancelar(1L, "x", admin));
    }
}
