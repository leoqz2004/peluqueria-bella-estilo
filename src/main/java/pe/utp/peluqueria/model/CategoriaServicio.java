package pe.utp.peluqueria.model;

import jakarta.persistence.*;

@Entity @Table(name = "categoria_servicio")
public class CategoriaServicio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_categoria")
    private Long id;
    private String nombre;
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
}
