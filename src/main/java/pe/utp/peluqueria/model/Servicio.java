package pe.utp.peluqueria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name = "servicio")
public class Servicio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_servicio")
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "id_categoria")
    private CategoriaServicio categoria;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    @Column(name = "duracion_min") private int duracionMin;
    private boolean activo = true;

    public Long getId() { return id; }
    public CategoriaServicio getCategoria() { return categoria; }
    public void setCategoria(CategoriaServicio c) { this.categoria = c; }
    public String getNombre() { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
    public String getDescripcion() { return descripcion == null ? "" : descripcion; }
    public void setDescripcion(String d) { this.descripcion = d; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal p) { this.precio = p; }
    public int getDuracionMin() { return duracionMin; }
    public void setDuracionMin(int d) { this.duracionMin = d; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean a) { this.activo = a; }
    @Override public boolean equals(Object o) { return o instanceof Servicio s && id != null && id.equals(s.id); }
    @Override public int hashCode() { return 31; }
}
