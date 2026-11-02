package pe.utp.peluqueria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "notificacion")
public class Notificacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_notificacion")
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "id_cita")
    private Cita cita;
    @Enumerated(EnumType.STRING) private TipoNotificacion tipo;
    @Enumerated(EnumType.STRING) private Canal canal;
    private String mensaje;
    @Column(name = "fecha_programada") private LocalDateTime fechaProgramada;
    @Column(name = "fecha_envio") private LocalDateTime fechaEnvio;
    @Enumerated(EnumType.STRING) @Column(name = "estado_envio") private EstadoEnvio estadoEnvio = EstadoEnvio.PENDIENTE;
    private int intentos;

    public Notificacion() {}
    public Notificacion(Cita c, TipoNotificacion t, Canal canal, String msg, LocalDateTime programada) {
        this.cita = c; this.tipo = t; this.canal = canal; this.mensaje = msg; this.fechaProgramada = programada;
    }
    public Long getId() { return id; }
    public LocalDateTime getFechaProgramada() { return fechaProgramada; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public Cita getCita() { return cita; }
    public Canal getCanal() { return canal; }
    public String getMensaje() { return mensaje; }
    public TipoNotificacion getTipo() { return tipo; }
    public EstadoEnvio getEstadoEnvio() { return estadoEnvio; }
    public void marcarEnviada() { this.estadoEnvio = EstadoEnvio.ENVIADA; this.fechaEnvio = LocalDateTime.now(); }
    public void registrarFallo() { this.intentos++; if (intentos >= 3) this.estadoEnvio = EstadoEnvio.FALLIDA; }
}
