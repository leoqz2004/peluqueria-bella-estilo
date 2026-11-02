package pe.utp.peluqueria.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.utp.peluqueria.model.Estilista;

public interface EstilistaRepository extends JpaRepository<Estilista, Long> {
    List<Estilista> findByActivoTrue();
}
