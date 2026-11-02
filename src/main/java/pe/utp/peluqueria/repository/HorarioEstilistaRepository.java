package pe.utp.peluqueria.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.utp.peluqueria.model.HorarioEstilista;

public interface HorarioEstilistaRepository extends JpaRepository<HorarioEstilista, Long> {
    List<HorarioEstilista> findByEstilistaIdAndDiaSemanaOrderByHoraInicio(Long estilistaId, int diaSemana);
    List<HorarioEstilista> findByEstilistaIdOrderByDiaSemana(Long estilistaId);
}
