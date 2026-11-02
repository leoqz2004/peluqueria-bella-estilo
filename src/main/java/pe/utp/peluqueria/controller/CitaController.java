package pe.utp.peluqueria.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.utp.peluqueria.dto.ReservaDto;
import pe.utp.peluqueria.model.Cita;
import pe.utp.peluqueria.model.Usuario;
import pe.utp.peluqueria.service.*;

@Controller
public class CitaController {
    private static final Set<String> DESTINOS = Set.of("/admin/citas", "/admin/panel", "/agenda", "/inicio");
    private final CitaService citas;
    private final CatalogoService catalogo;
    private final UsuarioService usuarios;

    public CitaController(CitaService citas, CatalogoService catalogo, UsuarioService usuarios) {
        this.citas = citas; this.catalogo = catalogo; this.usuarios = usuarios;
    }

    /** P04–P06: asistente de reserva (servicio → estilista → horario → confirmar). */
    @GetMapping("/reservar")
    public String asistente(@RequestParam(required = false) Long servicio, Model m) {
        m.addAttribute("servicios", catalogo.catalogoActivo());
        m.addAttribute("estilistas", catalogo.estilistasActivos());
        m.addAttribute("preselect", servicio);
        return "reservar";
    }

    /** JSON con las horas libres (paso 3 del asistente). */
    @GetMapping("/reservar/horarios") @ResponseBody
    public List<LocalTime> horarios(@RequestParam Long estilista,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                    @RequestParam List<Long> servicios) {
        return citas.horariosLibres(estilista, fecha, servicios);
    }

    @PostMapping("/reservar")
    public String reservar(@Valid @ModelAttribute ReservaDto dto, BindingResult br, Principal p) {
        if (br.hasErrors()) return "redirect:/reservar?error";
        citas.crearCita(usuarios.porEmail(p.getName()).getId(), dto);
        return "redirect:/mis-citas?creada";
    }

    /** P07: mis citas (próximas e historial). */
    @GetMapping("/mis-citas")
    public String misCitas(Model m, Principal p) {
        List<Cita> todas = citas.historial(usuarios.porEmail(p.getName()).getId());
        LocalDate hoy = LocalDate.now();
        m.addAttribute("proximas", todas.stream().filter(c -> c.isModificable() && !c.getFecha().isBefore(hoy)).toList());
        m.addAttribute("historial", todas.stream().filter(c -> !(c.isModificable() && !c.getFecha().isBefore(hoy))).toList());
        return "mis-citas";
    }

    @PostMapping("/citas/{id}/cancelar")
    public String cancelar(@PathVariable Long id, @RequestParam(required = false) String motivo,
                           @RequestParam(required = false) String volver, Principal p) {
        Usuario u = usuarios.porEmail(p.getName());
        citas.cancelar(id, motivo == null || motivo.isBlank() ? "Cancelada" : motivo.trim(), u);
        if ("ADMINISTRADOR".equals(u.getRol().getNombre())) return "redirect:" + (volver != null && DESTINOS.contains(volver) ? volver : "/admin/citas");
        return "redirect:/mis-citas?cancelada";
    }

    @PostMapping("/citas/{id}/reprogramar")
    public String reprogramar(@PathVariable Long id,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora, Principal p) {
        citas.reprogramar(id, fecha, hora, usuarios.porEmail(p.getName()));
        return "redirect:/mis-citas?reprogramada";
    }

    /** Horas libres para reprogramar una cita existente (modal de «Mis citas»). */
    @GetMapping("/citas/{id}/horarios") @ResponseBody
    public List<LocalTime> horariosCita(@PathVariable Long id,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha, Principal p) {
        return citas.horariosParaReprogramar(id, fecha, usuarios.porEmail(p.getName()));
    }
}
