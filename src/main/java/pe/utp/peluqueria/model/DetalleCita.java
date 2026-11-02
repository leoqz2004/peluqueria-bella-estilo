package pe.utp.peluqueria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Guarda precio y duracion vigentes al reservar: un cambio de tarifa no altera citas pasadas. */
@Entity @Table(name = "detalle_cita")
public class DetalleCita {
    @EmbeddedId private DetalleCitaId id = new DetalleCitaId();
    @MapsId("idCita") @ManyToOne(optional = false) @JoinColumn(name = "id_cita")
    private Cita cita;
    @MapsId("idServicio") @ManyToOne(optional = false) @JoinColumn(name = "id_servicio")
    private Servicio servicio;
    @Column(name = "precio_aplicado") private BigDecimal precioAplicado;
    @Column(name = "duracion_min") private int duracionMin;

    public DetalleCita() {}
    public DetalleCita(Cita cita, Servicio s) {
        this.cita = cita; this.servicio = s;
        this.precioAplicado = s.getPrecio(); this.duracionMin = s.getDuracionMin();
    }
    public Servicio getServicio() { return servicio; }
    public BigDecimal getPrecioAplicado() { return precioAplicado; }
    public int getDuracionMin() { return duracionMin; }
}
