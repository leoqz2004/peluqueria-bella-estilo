package pe.utp.peluqueria.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity @Table(name = "horario_estilista")
public class HorarioEstilista {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_horario")
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "id_estilista")
    private Estilista estilista;
    @Column(name = "dia_semana") private int diaSemana;   // 1 = lunes ... 7 = domingo
    @Column(name = "hora_inicio") private LocalTime horaInicio;
    @Column(name = "hora_fin") private LocalTime horaFin;

    public HorarioEstilista() {}
    public HorarioEstilista(Estilista e, int dia, LocalTime ini, LocalTime fin) {
        this.estilista = e; this.diaSemana = dia; this.horaInicio = ini; this.horaFin = fin;
    }
    private static final String[] DIAS = {"", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
    public String getDiaNombre() { return DIAS[diaSemana]; }
    public Long getId() { return id; }
    public Estilista getEstilista() { return estilista; }
    public int getDiaSemana() { return diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
}
