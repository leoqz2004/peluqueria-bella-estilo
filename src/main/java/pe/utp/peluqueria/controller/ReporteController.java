package pe.utp.peluqueria.controller;

import java.time.LocalDate;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.utp.peluqueria.model.EstadoCita;
import pe.utp.peluqueria.service.ReporteService;

/** P14 y P15: reportes de gestión. */
@Controller
@RequestMapping("/admin/reportes")
public class ReporteController {
    private final ReporteService reportes;
    public ReporteController(ReporteService reportes) { this.reportes = reportes; }

    @GetMapping("/citas")
    public String citas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta, Model m) {
        LocalDate h = hasta != null ? hasta : LocalDate.now().plusMonths(1);
        LocalDate d = desde != null ? desde : LocalDate.now().minusMonths(1);
        Map<EstadoCita, Long> porEstado = reportes.citasPorEstado(d, h);
        long total = porEstado.values().stream().mapToLong(Long::longValue).sum();
        m.addAttribute("desde", d); m.addAttribute("hasta", h);
        m.addAttribute("porEstado", porEstado);
        m.addAttribute("total", total);
        m.addAttribute("nAtendidas", porEstado.get(EstadoCita.ATENDIDA));
        m.addAttribute("nCanceladas", porEstado.get(EstadoCita.CANCELADA));
        m.addAttribute("nAusentes", porEstado.get(EstadoCita.AUSENTE));
        m.addAttribute("max", Math.max(1, porEstado.values().stream().mapToLong(Long::longValue).max().orElse(1)));
        return "reportes/citas";
    }

    @GetMapping("/servicios")
    public String servicios(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta, Model m) {
        LocalDate h = hasta != null ? hasta : LocalDate.now().plusMonths(1);
        LocalDate d = desde != null ? desde : LocalDate.now().minusMonths(1);
        var ranking = reportes.serviciosMasSolicitados();
        long max = ranking.stream().mapToLong(r -> (Long) r[1]).max().orElse(1);
        var minutos = reportes.minutosPorEstilista(d, h);
        m.addAttribute("ranking", ranking); m.addAttribute("max", Math.max(1, max));
        m.addAttribute("minutos", minutos);
        m.addAttribute("maxMin", Math.max(1, minutos.values().stream().mapToLong(Long::longValue).max().orElse(1)));
        m.addAttribute("desde", d); m.addAttribute("hasta", h);
        return "reportes/servicios";
    }
}
