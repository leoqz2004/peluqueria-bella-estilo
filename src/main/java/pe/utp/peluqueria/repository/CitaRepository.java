package pe.utp.peluqueria.repository;

import jakarta.persistence.LockModeType;
import java.time.*;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.utp.peluqueria.model.*;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    /** RN01 con bloqueo pesimista: evita dos reservas simultaneas sobre el mismo rango. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select c from Cita c
        where c.estilista.id = :estilista and c.fecha = :fecha
          and c.estado <> pe.utp.peluqueria.model.EstadoCita.CANCELADA
          and c.horaInicio < :fin and c.horaFin > :inicio
          and (:excluir is null or c.id <> :excluir)""")
    List<Cita> buscarCrucesConBloqueo(@Param("estilista") Long estilista, @Param("fecha") LocalDate fecha,
                                      @Param("inicio") LocalTime inicio, @Param("fin") LocalTime fin,
                                      @Param("excluir") Long excluir);

    @Query("""
        select c from Cita c
        where c.estilista.id = :estilista and c.fecha = :fecha
          and c.estado <> pe.utp.peluqueria.model.EstadoCita.CANCELADA""")
    List<Cita> activasDelDia(@Param("estilista") Long estilista, @Param("fecha") LocalDate fecha);

    @Query("""
        select distinct c from Cita c join fetch c.estilista join fetch c.sede
        left join fetch c.detalles d left join fetch d.servicio
        where c.cliente.id = :cliente order by c.fecha desc, c.horaInicio desc""")
    List<Cita> historialDeCliente(@Param("cliente") Long cliente);

    @Query("""
        select distinct c from Cita c join fetch c.cliente join fetch c.estilista
        left join fetch c.detalles d left join fetch d.servicio
        where c.fecha = :fecha and c.estilista.id = :estilista order by c.horaInicio""")
    List<Cita> agendaDelEstilista(@Param("estilista") Long estilista, @Param("fecha") LocalDate fecha);

    @Query("""
        select distinct c from Cita c join fetch c.cliente join fetch c.estilista
        left join fetch c.detalles d left join fetch d.servicio
        where c.fecha between :desde and :hasta order by c.fecha, c.horaInicio""")
    List<Cita> entreFechas(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    // R1
    @Query("select c.estado, count(c) from Cita c where c.fecha between :desde and :hasta group by c.estado")
    List<Object[]> contarPorEstado(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    // R2
    @Query("""
        select s.nombre, count(d) from DetalleCita d join d.servicio s join d.cita c
        where c.estado in (pe.utp.peluqueria.model.EstadoCita.CONFIRMADA, pe.utp.peluqueria.model.EstadoCita.ATENDIDA)
        group by s.nombre order by count(d) desc""")
    List<Object[]> serviciosMasSolicitados();

    // R3 (se suman los minutos en Java)
    List<Cita> findByEstadoAndFechaBetween(EstadoCita estado, LocalDate desde, LocalDate hasta);

    // Para el listado de clientes: [idCliente, cantidad, ultimaFecha]
    @Query("select c.cliente.id, count(c), max(c.fecha) from Cita c group by c.cliente.id")
    List<Object[]> resumenPorCliente();
}
