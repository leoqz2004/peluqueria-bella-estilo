package pe.utp.peluqueria.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PrivacidadTest {
    @Test void enmascaraCorreoNormal() { assertEquals("v*****@correo.com", Privacidad.enmascararEmail("valeria.rojas@correo.com")); }
    @Test void enmascaraCorreoCorto()  { assertEquals("a@x.pe", Privacidad.enmascararEmail("a@x.pe")); }
    @Test void valoresInvalidos() {
        assertEquals("***", Privacidad.enmascararEmail(null));
        assertEquals("***", Privacidad.enmascararEmail("  "));
        assertEquals("***", Privacidad.enmascararEmail("sin-arroba"));
    }
}
