package pe.utp.peluqueria.service;

import jakarta.servlet.http.HttpSession;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.utp.peluqueria.dto.ReservaDto;
import pe.utp.peluqueria.dto.ReservaPendiente;

/**
 * Implementacion basada en la sesion HTTP. No toca la base de datos: la cita real solo se crea
 * cuando un CLIENTE autenticado confirma (RN01-RN03 siguen validandose en {@code CitaService}).
 * La vigencia se configura con {@code app.reserva-pendiente.minutos}.
 */
@Service
public class ReservaPendienteSesionService implements ReservaPendienteService {
    static final String ATRIBUTO = "reservaPendiente";

    private final Duration vigencia;
    private final Clock reloj;

    @Autowired
    public ReservaPendienteSesionService(@Value("${app.reserva-pendiente.minutos:30}") int minutos) {
        this(minutos, Clock.systemDefaultZone());
    }

    /** Constructor para pruebas: permite controlar el reloj. */
    ReservaPendienteSesionService(int minutos, Clock reloj) {
        this.vigencia = Duration.ofMinutes(minutos);
        this.reloj = reloj;
    }

    @Override
    public void guardar(HttpSession sesion, ReservaDto dto) {
        sesion.setAttribute(ATRIBUTO, ReservaPendiente.de(dto, Instant.now(reloj)));
    }

    @Override
    public Optional<ReservaPendiente> obtenerVigente(HttpSession sesion) {
        if (sesion == null) return Optional.empty();
        Object valor = sesion.getAttribute(ATRIBUTO);
        if (!(valor instanceof ReservaPendiente pendiente)) return Optional.empty();
        if (Instant.now(reloj).isAfter(pendiente.getCreada().plus(vigencia))) {
            sesion.removeAttribute(ATRIBUTO);
            return Optional.empty();
        }
        return Optional.of(pendiente);
    }

    @Override
    public void descartar(HttpSession sesion) {
        if (sesion != null) sesion.removeAttribute(ATRIBUTO);
    }
}
