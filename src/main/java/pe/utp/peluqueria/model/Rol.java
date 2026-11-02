package pe.utp.peluqueria.model;

import jakarta.persistence.*;

@Entity @Table(name = "rol")
public class Rol {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_rol")
    private Integer id;
    @Column(nullable = false, unique = true, length = 20)
    private String nombre;

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
