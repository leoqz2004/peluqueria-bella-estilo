package pe.utp.peluqueria.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.utp.peluqueria.dto.RegistroDto;
import pe.utp.peluqueria.exception.ReglaNegocioException;
import pe.utp.peluqueria.service.UsuarioService;

@Controller
public class AuthController {
    private final UsuarioService usuarios;
    public AuthController(UsuarioService usuarios) { this.usuarios = usuarios; }

    @GetMapping("/login") public String login() { return "login"; }

    /** Redirige segun el rol tras iniciar sesion. */
    @GetMapping("/inicio")
    public String inicio(Authentication auth) {
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"))) return "redirect:/admin/panel";
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ESTILISTA"))) return "redirect:/agenda";
        return "redirect:/servicios";
    }
    @GetMapping("/") public String raiz() { return "redirect:/inicio"; }

    @GetMapping("/registro")
    public String form(Model m) { m.addAttribute("registro", new RegistroDto()); return "registro"; }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registro") RegistroDto dto, BindingResult br) {
        if (dto.getPassword() != null && !dto.getPassword().equals(dto.getConfirmPassword()))
            br.rejectValue("confirmPassword", "noCoincide", "Las contraseñas no coinciden");
        if (br.hasErrors()) return "registro";
        try { usuarios.registrarCliente(dto); }
        catch (ReglaNegocioException ex) { br.rejectValue("email", "duplicado", ex.getMessage()); return "registro"; }
        return "redirect:/login?registrado";
    }
}
