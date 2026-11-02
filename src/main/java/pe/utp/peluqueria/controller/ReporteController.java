package pe.utp.peluqueria.controller;

import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.utp.peluqueria.model.EstadoCita;
import pe.utp.peluqueria.service.ExcelExportService;
import pe.utp.peluqueria.service.ReporteService;

/** P14 y P15: reportes de gestión. */
@Controller
@RequestMapping("/admin/reportes")
public class ReporteController {
    private final ReporteService reportes;
    private final ExcelExportService excel;
    public ReporteController(ReporteService reportes, ExcelExportService excel) { this.reportes = reportes; this.excel = excel; }

    /** Exporta el reporte de gestion del periodo a Excel (Apache POI). Solo ADMINISTRADOR (ver SecurityConfig). */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate h = hasta != null ? hasta : LocalDate.now().plusMonths(1);
        LocalDate d = desde != null ? desde : LocalDate.now().minusMonths(1);
        byte[] libro = excel.reporteGestion(d, h, reportes.citasPorEstado(d, h),
                                            reportes.serviciosMasSolicitados(), reportes.minutosPorEstilista(d, h));
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename("reporte-bella-estilo-" + d + "_" + h + ".xlsx").build().toString())
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(libro);
    }

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
