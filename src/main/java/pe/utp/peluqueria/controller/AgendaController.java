package pe.utp.peluqueria.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.utp.peluqueria.model.Cita;
import pe.utp.peluqueria.model.EstadoCita;
import pe.utp.peluqueria.service.*;

/** P16: agenda del estilista. */
@Controller
@RequestMapping("/agenda")
public class AgendaController {
    private final CitaService citas;
    private final UsuarioService usuarios;
    public AgendaController(CitaService citas, UsuarioService usuarios) { this.citas = citas; this.usuarios = usuarios; }

    @GetMapping
    public String agenda(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                         Model m, Principal p) {
        LocalDate f = fecha != null ? fecha : LocalDate.now();
        List<Cita> todas = citas.agenda(usuarios.porEmail(p.getName()).getId(), f);
        m.addAttribute("fecha", f);
        m.addAttribute("pendientes", todas.stream().filter(c -> c.getEstado() == EstadoCita.PENDIENTE || c.getEstado() == EstadoCita.CONFIRMADA).toList());
        m.addAttribute("atendidas", todas.stream().filter(c -> c.getEstado() == EstadoCita.ATENDIDA || c.getEstado() == EstadoCita.AUSENTE).toList());
        return "agenda";
    }

    @PostMapping("/{id}/atendida")
    public String resultado(@PathVariable Long id, @RequestParam EstadoCita resultado, Principal p) {
        citas.marcarResultado(id, resultado, usuarios.porEmail(p.getName()));
        return "redirect:/agenda";
    }
}
