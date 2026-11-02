package pe.utp.peluqueria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DetalleCitaId implements Serializable {
    @Column(name = "id_cita") private Long idCita;
    @Column(name = "id_servicio") private Long idServicio;

    public DetalleCitaId() {}
    public DetalleCitaId(Long idCita, Long idServicio) { this.idCita = idCita; this.idServicio = idServicio; }
    @Override public boolean equals(Object o) {
        return o instanceof DetalleCitaId d && Objects.equals(idCita, d.idCita) && Objects.equals(idServicio, d.idServicio);
    }
    @Override public int hashCode() { return Objects.hash(idCita, idServicio); }
}
