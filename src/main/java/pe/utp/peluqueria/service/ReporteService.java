package pe.utp.peluqueria.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.CitaRepository;

@Service
@Transactional(readOnly = true)
public class ReporteService {
    private final CitaRepository citas;
    public ReporteService(CitaRepository citas) { this.citas = citas; }

    /** R1: citas por estado en un periodo. */
    public Map<EstadoCita, Long> citasPorEstado(LocalDate desde, LocalDate hasta) {
        Map<EstadoCita, Long> r = new EnumMap<>(EstadoCita.class);
        for (EstadoCita e : EstadoCita.values()) r.put(e, 0L);
        for (Object[] fila : citas.contarPorEstado(desde, hasta)) r.put((EstadoCita) fila[0], (Long) fila[1]);
        return r;
    }

    /** R2: servicios mas solicitados. */
    public List<Object[]> serviciosMasSolicitados() { return citas.serviciosMasSolicitados(); }

    /** R3: minutos atendidos por estilista. */
    public Map<String, Long> minutosPorEstilista(LocalDate desde, LocalDate hasta) {
        return citas.findByEstadoAndFechaBetween(EstadoCita.ATENDIDA, desde, hasta).stream()
            .collect(Collectors.groupingBy(c -> c.getEstilista().getNombreCompleto(), TreeMap::new,
                     Collectors.summingLong(c -> Duration.between(c.getHoraInicio(), c.getHoraFin()).toMinutes())));
    }
}
