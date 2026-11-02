package pe.utp.peluqueria.controller;

import java.security.Principal;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.utp.peluqueria.model.Usuario;
import pe.utp.peluqueria.service.NotificacionService;
import pe.utp.peluqueria.service.UsuarioService;
import pe.utp.peluqueria.util.Acceso;

/** P08: historial de avisos y preferencias de recordatorio. */
@Controller
public class NotificacionController {
    private final NotificacionService notificaciones;
    private final UsuarioService usuarios;
    public NotificacionController(NotificacionService n, UsuarioService u) { this.notificaciones = n; this.usuarios = u; }

    @GetMapping("/notificaciones")
    public String ver(Model m, Principal p, Authentication auth) {
        if (Acceso.autenticado(auth) && !Acceso.esCliente(auth)) return "redirect:/inicio";
        if (p == null) {                       // invitado: no hay avisos de WhatsApp/correo hasta iniciar sesion
            m.addAttribute("avisos", List.of());
            return "notificaciones";
        }
        Usuario u = usuarios.porEmail(p.getName());
        m.addAttribute("avisos", notificaciones.deCliente(u.getId()));
        m.addAttribute("cliente", u);
        return "notificaciones";
    }

    @PostMapping("/notificaciones/preferencias")
    public String guardar(@RequestParam(defaultValue = "false") boolean whatsapp,
                          @RequestParam(defaultValue = "false") boolean correo, Principal p) {
        usuarios.actualizarPreferencias(usuarios.porEmail(p.getName()).getId(), whatsapp, correo);
        return "redirect:/notificaciones?guardado";
    }
}
