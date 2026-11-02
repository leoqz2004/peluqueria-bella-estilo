package pe.utp.peluqueria.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.utp.peluqueria.model.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    List<Servicio> findByActivoTrueOrderByCategoriaNombreAscNombreAsc();   // RN07
}
