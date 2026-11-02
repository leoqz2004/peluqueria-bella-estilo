package pe.utp.peluqueria.controller;

import java.security.Principal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pe.utp.peluqueria.model.Usuario;
import pe.utp.peluqueria.service.UsuarioService;

/** Expone el usuario autenticado como «yo» en todas las vistas (barra superior / menu lateral). */
@ControllerAdvice
public class ModeloGlobal {
    private final UsuarioService usuarios;
    public ModeloGlobal(UsuarioService usuarios) { this.usuarios = usuarios; }

    @ModelAttribute("yo")
    public Usuario yo(Principal principal) {
        return principal == null ? null : usuarios.porEmail(principal.getName());
    }
}
