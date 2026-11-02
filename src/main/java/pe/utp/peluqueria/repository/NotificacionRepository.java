package pe.utp.peluqueria.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.utp.peluqueria.model.*;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByEstadoEnvioAndFechaProgramadaLessThanEqual(EstadoEnvio estado, LocalDateTime hasta);
    void deleteByCitaIdAndTipoAndEstadoEnvio(Long citaId, TipoNotificacion tipo, EstadoEnvio estado);

    @Query("select n from Notificacion n where n.cita.cliente.id = :cliente order by n.fechaProgramada desc")
    List<Notificacion> deCliente(@Param("cliente") Long cliente);
}
