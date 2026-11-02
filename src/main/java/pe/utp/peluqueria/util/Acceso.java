package pe.utp.peluqueria.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Consultas puras sobre la autenticacion actual. Centraliza la regla "invitado / cliente / otro rol"
 * para que los controladores y el manejador de login no repitan la logica (principio DRY y SRP).
 */
public final class Acceso {
    private Acceso() { }

    /** @return {@code true} si hay un usuario realmente autenticado (no invitado). */
    public static boolean autenticado(Authentication a) {
        return a != null && a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken);
    }

    /** @return {@code true} si el usuario autenticado tiene el rol CLIENTE. */
    public static boolean esCliente(Authentication a) {
        return autenticado(a) && a.getAuthorities().stream().anyMatch(g -> "ROLE_CLIENTE".equals(g.getAuthority()));
    }
}
