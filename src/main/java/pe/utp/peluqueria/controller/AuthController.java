package pe.utp.peluqueria.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.utp.peluqueria.dto.RegistroDto;
import pe.utp.peluqueria.exception.ReglaNegocioException;
import pe.utp.peluqueria.service.ReservaPendienteService;
import pe.utp.peluqueria.service.UsuarioService;
import pe.utp.peluqueria.util.Acceso;

@Controller
public class AuthController {
    private final UsuarioService usuarios;
    private final ReservaPendienteService pendientes;
    public AuthController(UsuarioService usuarios, ReservaPendienteService pendientes) {
        this.usuarios = usuarios; this.pendientes = pendientes;
    }

    @GetMapping("/login") public String login() { return "login"; }

    /** Redirige segun el rol tras iniciar sesion. */
    @GetMapping("/inicio")
    public String inicio(Authentication auth) {
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"))) return "redirect:/admin/panel";
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ESTILISTA"))) return "redirect:/agenda";
        return "redirect:/servicios";
    }
    /** Pantalla de arranque: un invitado entra al catalogo del cliente; con sesion se enruta por rol. */
    @GetMapping("/")
    public String raiz(Authentication auth) { return Acceso.autenticado(auth) ? "redirect:/inicio" : "redirect:/servicios"; }

    @GetMapping("/registro")
    public String form(Model m) { m.addAttribute("registro", new RegistroDto()); return "registro"; }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registro") RegistroDto dto, BindingResult br, HttpSession sesion) {
        if (dto.getPassword() != null && !dto.getPassword().equals(dto.getConfirmPassword()))
            br.rejectValue("confirmPassword", "noCoincide", "Las contraseñas no coinciden");
        if (br.hasErrors()) return "registro";
        try { usuarios.registrarCliente(dto); }
        catch (ReglaNegocioException ex) { br.rejectValue("email", "duplicado", ex.getMessage()); return "registro"; }
        // Si el invitado venia de elegir una cita, se le recuerda en el login que la conservamos.
        return pendientes.obtenerVigente(sesion).isPresent() ? "redirect:/login?registrado&reservar" : "redirect:/login?registrado";
    }
}
