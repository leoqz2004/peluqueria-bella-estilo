package pe.utp.peluqueria.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Prueba de integracion del flujo: invitado -> catalogo -> reserva -> "Confirmar cita" -> login -> retomar.
 * Usa el perfil demo (H2 en memoria con usuarios de ejemplo).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class AccesoInvitadoIntegrationTest {
    @Autowired MockMvc mvc;

    private MockHttpServletRequestBuilder confirmarComoInvitado() {
        return post("/reservar/pendiente").with(csrf())
            .param("estilistaId", "1").param("servicioIds", "1")
            .param("fecha", LocalDate.now().plusDays(1).toString()).param("hora", "10:00");
    }

    @Test
    void laAppArrancaEnElCatalogoSinIniciarSesion() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/servicios"));
        mvc.perform(get("/servicios")).andExpect(status().isOk());
        mvc.perform(get("/reservar")).andExpect(status().isOk());
    }

    @Test
    void invitadoNoVeNotificacionesDeWhatsAppNiCorreo() throws Exception {
        String html = mvc.perform(get("/notificaciones")).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(html.contains("Inicia sesión para ver tus notificaciones"));
        assertFalse(html.contains("Preferencias de recordatorio"));
    }

    @Test
    void zonasPrivadasSiguenExigiendoLogin() throws Exception {
        mvc.perform(get("/mis-citas")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/admin/panel")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(post("/reservar").with(csrf()).param("estilistaId", "1").param("servicioIds", "1")
                .param("fecha", LocalDate.now().plusDays(1).toString()).param("hora", "10:00"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void confirmarSinSesionLlevaAlLoginYGuardaLaSeleccion() throws Exception {
        MockHttpSession sesion = (MockHttpSession) mvc.perform(confirmarComoInvitado())
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?reservar"))
            .andReturn().getRequest().getSession();
        assertNotNull(sesion.getAttribute("reservaPendiente"));

        String login = mvc.perform(get("/login?reservar")).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(login.contains("primero debes iniciar sesión"));
    }

    @Test
    void clienteQueIniciaSesionRetomaLaReserva() throws Exception {
        MockHttpSession sesion = (MockHttpSession) mvc.perform(confirmarComoInvitado()).andReturn().getRequest().getSession();

        mvc.perform(post("/login").session(sesion).with(csrf())
                .param("username", "valeria.rojas@correo.com").param("password", "Cliente123!"))
            .andExpect(redirectedUrl("/reservar?retomar=1"));

        String html = mvc.perform(get("/reservar?retomar=1").session(sesion)).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(html.contains("data-pend-estilista=\"1\""), "el asistente recibe la seleccion guardada");
    }

    @Test
    void administradorQueIniciaSesionVaASuPanelSinConservarLaReserva() throws Exception {
        MockHttpSession sesion = (MockHttpSession) mvc.perform(confirmarComoInvitado()).andReturn().getRequest().getSession();

        mvc.perform(post("/login").session(sesion).with(csrf())
                .param("username", "recepcion@bellaestilo.pe").param("password", "Admin123!"))
            .andExpect(redirectedUrl("/inicio"));
        assertNull(sesion.getAttribute("reservaPendiente"));
    }

    @Test @WithUserDetails("milagros@bellaestilo.pe")
    void estilistaNoEntraAlFlujoDeCliente() throws Exception {
        mvc.perform(get("/servicios")).andExpect(redirectedUrl("/inicio"));
        mvc.perform(get("/reservar")).andExpect(redirectedUrl("/inicio"));
        mvc.perform(get("/")).andExpect(redirectedUrl("/inicio"));
    }

    @Test @WithUserDetails("recepcion@bellaestilo.pe")
    void administradorSigueEntrandoASuPanel() throws Exception {
        mvc.perform(get("/admin/panel")).andExpect(status().isOk());
        mvc.perform(get("/servicios")).andExpect(redirectedUrl("/inicio"));
    }

    @Test @WithUserDetails("valeria.rojas@correo.com")
    void clienteConSesionVeSuMenuCompleto() throws Exception {
        String html = mvc.perform(get("/servicios")).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(html.contains("Mis citas"));
        mvc.perform(get("/mis-citas")).andExpect(status().isOk());
        mvc.perform(post("/logout").with(csrf())).andExpect(redirectedUrl("/servicios?salir"));
    }

    @Test @WithUserDetails("recepcion@bellaestilo.pe")
    void administradorDescargaElReporteExcel() throws Exception {
        mvc.perform(get("/admin/reportes/excel")).andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("reporte-bella-estilo-")));
    }
}
