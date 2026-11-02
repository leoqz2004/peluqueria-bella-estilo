package pe.utp.peluqueria.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalTime;
import java.util.List;

public class ReservaDto {
    @NotNull private Long estilistaId;
    @NotEmpty private List<Long> servicioIds;
    @NotNull @FutureOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate fecha;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) private LocalTime hora;

    public Long getEstilistaId() { return estilistaId; }
    public void setEstilistaId(Long v) { this.estilistaId = v; }
    public List<Long> getServicioIds() { return servicioIds; }
    public void setServicioIds(List<Long> v) { this.servicioIds = v; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate v) { this.fecha = v; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime v) { this.hora = v; }
}
