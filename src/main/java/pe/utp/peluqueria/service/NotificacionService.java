package pe.utp.peluqueria.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.NotificacionRepository;

@Service
public class NotificacionService {
    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificacionRepository repo;
    private final int[] horasRecordatorio;

    public NotificacionService(NotificacionRepository repo, @Value("${app.recordatorio.horas}") int[] horas) {
        this.repo = repo;
        this.horasRecordatorio = horas;
    }

    /** RN05: cada cambio de la cita genera una notificacion por cada canal aceptado por el cliente. */
    @Transactional
    public void registrar(Cita cita, TipoNotificacion tipo) {
        programar(cita, tipo, LocalDateTime.now(), mensaje(cita, tipo));
    }

    /** RN06: recordatorios 24 h y 2 h antes (solo canales aceptados). */
    @Transactional
    public void programarRecordatorios(Cita cita) {
        for (int h : horasRecordatorio) {
            LocalDateTime cuando = cita.getInicio().minusHours(h);
            if (cuando.isAfter(LocalDateTime.now())) {
                programar(cita, TipoNotificacion.RECORDATORIO, cuando,
                          "Recordatorio: tu cita es el " + cita.getInicio().format(FMT));
            }
        }
    }

    @Transactional
    public void anularRecordatoriosPendientes(Cita cita) {
        repo.deleteByCitaIdAndTipoAndEstadoEnvio(cita.getId(), TipoNotificacion.RECORDATORIO, EstadoEnvio.PENDIENTE);
    }

    private void programar(Cita cita, TipoNotificacion tipo, LocalDateTime cuando, String msg) {
        Cliente c = cita.getCliente();
        if (c.isAceptaWhatsapp()) repo.save(new Notificacion(cita, tipo, Canal.WHATSAPP, msg, cuando));
        if (c.isAceptaCorreo())   repo.save(new Notificacion(cita, tipo, Canal.CORREO, msg, cuando));
    }

    private String mensaje(Cita c, TipoNotificacion t) {
        String cuando = c.getInicio().format(FMT);
        return switch (t) {
            case REGISTRO -> "Tu cita del " + cuando + " fue registrada y esta pendiente de confirmacion.";
            case CONFIRMACION -> "Tu cita del " + cuando + " fue confirmada.";
            case CANCELACION -> "Tu cita del " + cuando + " fue cancelada.";
            case REPROGRAMACION -> "Tu cita fue reprogramada para el " + cuando + ".";
            case RECORDATORIO -> "Recordatorio: tu cita es el " + cuando;
        };
    }

    @Transactional(readOnly = true)
    public java.util.List<Notificacion> deCliente(Long clienteId) { return repo.deCliente(clienteId); }

    /** Punto de integracion: aqui se invocaria la API de WhatsApp Business o JavaMailSender. */
    @Transactional
    public void enviar(Notificacion n) {
        try {
            log.info("[{}] {}", n.getCanal(), n.getMensaje());
            n.marcarEnviada();
        } catch (RuntimeException ex) {
            n.registrarFallo();
        }
    }
}
