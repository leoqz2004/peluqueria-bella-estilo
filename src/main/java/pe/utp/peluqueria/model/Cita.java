package pe.utp.peluqueria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "cita")
public class Cita {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_cita")
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "id_cliente")
    private Cliente cliente;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "id_estilista")
    private Estilista estilista;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "id_sede")
    private Sede sede;
    private LocalDate fecha;
    @Column(name = "hora_inicio") private LocalTime horaInicio;
    @Column(name = "hora_fin") private LocalTime horaFin;
    @Enumerated(EnumType.STRING) private EstadoCita estado = EstadoCita.PENDIENTE;
    private BigDecimal total;
    @Column(name = "motivo_cancelacion") private String motivoCancelacion;
    @Column(name = "fecha_creacion", insertable = false, updatable = false) private LocalDateTime fechaCreacion;
    @OneToMany(mappedBy = "cita", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCita> detalles = new ArrayList<>();

    public LocalDateTime getInicio() { return LocalDateTime.of(fecha, horaInicio); }
    public String getServiciosTexto() {
        return detalles.stream().map(d -> d.getServicio().getNombre()).collect(java.util.stream.Collectors.joining(", "));
    }
    public long getDuracionMin() { return Duration.between(horaInicio, horaFin).toMinutes(); }
    public boolean isModificable() { return estado == EstadoCita.PENDIENTE || estado == EstadoCita.CONFIRMADA; }
    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente c) { this.cliente = c; }
    public Estilista getEstilista() { return estilista; }
    public void setEstilista(Estilista e) { this.estilista = e; }
    public Sede getSede() { return sede; }
    public void setSede(Sede s) { this.sede = s; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate f) { this.fecha = f; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime h) { this.horaInicio = h; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime h) { this.horaFin = h; }
    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita e) { this.estado = e; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal t) { this.total = t; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String m) { this.motivoCancelacion = m; }
    public List<DetalleCita> getDetalles() { return detalles; }
}
