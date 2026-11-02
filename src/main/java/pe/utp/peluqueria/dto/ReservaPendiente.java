package pe.utp.peluqueria.dto;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Seleccion de reserva (servicios, estilista, fecha y hora) que un invitado hizo antes de iniciar sesion.
 * Es inmutable y serializable para vivir de forma segura en la sesion HTTP. Usa Google Guava
 * ({@link ImmutableList}, {@link Preconditions}, {@link Joiner}).
 */
public final class ReservaPendiente implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Long estilistaId;
    private final List<Long> servicioIds;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final Instant creada;

    private ReservaPendiente(Long estilistaId, List<Long> servicioIds, LocalDate fecha, LocalTime hora, Instant creada) {
        Preconditions.checkArgument(estilistaId != null, "El estilista es obligatorio");
        Preconditions.checkArgument(servicioIds != null && !servicioIds.isEmpty(), "Debe haber al menos un servicio");
        Preconditions.checkArgument(fecha != null && hora != null, "Fecha y hora son obligatorias");
        this.estilistaId = estilistaId;
        this.servicioIds = ImmutableList.copyOf(servicioIds);
        this.fecha = fecha;
        this.hora = hora;
        this.creada = creada;
    }

    /** Crea una copia inmutable a partir del formulario ya validado. */
    public static ReservaPendiente de(ReservaDto dto, Instant creada) {
        return new ReservaPendiente(dto.getEstilistaId(), dto.getServicioIds(), dto.getFecha(), dto.getHora(), creada);
    }

    public Long getEstilistaId() { return estilistaId; }
    public List<Long> getServicioIds() { return servicioIds; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public Instant getCreada() { return creada; }

    /** Ids de servicio separados por coma (para el atributo data- que lee reservar.js). */
    public String getServiciosCsv() { return Joiner.on(',').join(servicioIds); }
}
