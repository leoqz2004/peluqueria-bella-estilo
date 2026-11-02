package pe.utp.peluqueria.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name = "estilista")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Estilista extends Usuario {
    @ManyToOne(optional = false) @JoinColumn(name = "id_sede")
    private Sede sede;
    private String especialidad;
    @ManyToMany
    @JoinTable(name = "estilista_servicio",
        joinColumns = @JoinColumn(name = "id_estilista"),
        inverseJoinColumns = @JoinColumn(name = "id_servicio"))
    private Set<Servicio> servicios = new HashSet<>();

    public boolean ofrece(Servicio s) { return servicios.contains(s); }
    /** IDs de servicios separados por coma (lo usa el asistente de reserva en el navegador). */
    public String getServicioIdsCsv() {
        return servicios.stream().map(s -> String.valueOf(s.getId())).sorted().collect(java.util.stream.Collectors.joining(","));
    }
    public Sede getSede() { return sede; }
    public void setSede(Sede sede) { this.sede = sede; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String v) { this.especialidad = v; }
    public Set<Servicio> getServicios() { return servicios; }
}
