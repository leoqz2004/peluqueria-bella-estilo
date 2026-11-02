package pe.utp.peluqueria.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ServicioDto {
    @NotBlank @Size(max = 80) private String nombre;
    @Size(max = 200) private String descripcion;
    @NotNull @DecimalMin("0.0") private BigDecimal precio;
    @NotNull @Min(5) private Integer duracionMin;
    @NotNull private Long categoriaId;

    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal v) { this.precio = v; }
    public Integer getDuracionMin() { return duracionMin; }
    public void setDuracionMin(Integer v) { this.duracionMin = v; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long v) { this.categoriaId = v; }
}
