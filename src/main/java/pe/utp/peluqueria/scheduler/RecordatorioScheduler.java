package pe.utp.peluqueria.scheduler;

import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.model.EstadoEnvio;
import pe.utp.peluqueria.repository.NotificacionRepository;
import pe.utp.peluqueria.service.NotificacionService;

/** Cada minuto despacha las notificaciones pendientes cuya fecha programada ya llego. */
@Component
public class RecordatorioScheduler {
    private final NotificacionRepository repo;
    private final NotificacionService servicio;

    public RecordatorioScheduler(NotificacionRepository repo, NotificacionService servicio) {
        this.repo = repo; this.servicio = servicio;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void despachar() {
        repo.findByEstadoEnvioAndFechaProgramadaLessThanEqual(EstadoEnvio.PENDIENTE, LocalDateTime.now())
            .forEach(servicio::enviar);
    }
}
