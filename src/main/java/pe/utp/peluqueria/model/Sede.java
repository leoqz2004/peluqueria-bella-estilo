package pe.utp.peluqueria.model;

import jakarta.persistence.*;

@Entity @Table(name = "sede")
public class Sede {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_sede")
    private Long id;
    private String nombre;
    private String direccion;
    private String telefono;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String d) { this.direccion = d; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String t) { this.telefono = t; }
}
