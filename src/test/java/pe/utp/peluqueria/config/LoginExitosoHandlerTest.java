package pe.utp.peluqueria.config;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pe.utp.peluqueria.dto.ReservaDto;
import pe.utp.peluqueria.service.ReservaPendienteSesionService;

/** Regla clave del cambio: la reserva guardada solo se conserva si quien inicia sesion es CLIENTE. */
class LoginExitosoHandlerTest {
    ReservaPendienteSesionService pendientes = new ReservaPendienteSesionService(30);
    LoginExitosoHandler handler = new LoginExitosoHandler(pendientes);
    MockHttpServletRequest req; MockHttpServletResponse res; MockHttpSession sesion;

    @BeforeEach
    void preparar() {
        sesion = new MockHttpSession();
        req = new MockHttpServletRequest(); req.setSession(sesion);
        res = new MockHttpServletResponse();
        ReservaDto d = new ReservaDto();
        d.setEstilistaId(1L); d.setServicioIds(List.of(1L)); d.setFecha(LocalDate.now().plusDays(1)); d.setHora(LocalTime.of(10, 0));
        pendientes.guardar(sesion, d);
    }

    private UsernamePasswordAuthenticationToken usuario(String correo, String rol) {
        return UsernamePasswordAuthenticationToken.authenticated(correo, "x", List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
    }

    @Test
    void clienteConReservaVuelveAConfirmar() throws Exception {
        handler.onAuthenticationSuccess(req, res, usuario("valeria.rojas@correo.com", "CLIENTE"));
        assertEquals("/reservar?retomar=1", res.getRedirectedUrl());
        assertTrue(pendientes.obtenerVigente(sesion).isPresent(), "la reserva se conserva para el cliente");
    }

    @Test
    void administradorVaASuPanelYSeDescartaLaReserva() throws Exception {
        handler.onAuthenticationSuccess(req, res, usuario("recepcion@bellaestilo.pe", "ADMINISTRADOR"));
        assertEquals("/inicio", res.getRedirectedUrl());
        assertTrue(pendientes.obtenerVigente(sesion).isEmpty());
    }

    @Test
    void estilistaVaASuAgendaYSeDescartaLaReserva() throws Exception {
        handler.onAuthenticationSuccess(req, res, usuario("milagros@bellaestilo.pe", "ESTILISTA"));
        assertEquals("/inicio", res.getRedirectedUrl());
        assertTrue(pendientes.obtenerVigente(sesion).isEmpty());
    }

    @Test
    void clienteSinReservaVaAlInicio() throws Exception {
        pendientes.descartar(sesion);
        handler.onAuthenticationSuccess(req, res, usuario("valeria.rojas@correo.com", "CLIENTE"));
        assertEquals("/inicio", res.getRedirectedUrl());
    }
}
