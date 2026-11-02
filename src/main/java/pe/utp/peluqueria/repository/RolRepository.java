package pe.utp.peluqueria.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.utp.peluqueria.model.Rol;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombre(String nombre);
}
