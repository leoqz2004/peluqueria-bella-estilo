package pe.utp.peluqueria.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.utp.peluqueria.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    @Query("""
        select c from Cliente c
        where lower(c.nombres) like lower(concat('%', :q, '%'))
           or lower(c.apellidos) like lower(concat('%', :q, '%'))
           or lower(c.email) like lower(concat('%', :q, '%'))
        order by c.apellidos, c.nombres""")
    List<Cliente> buscar(@Param("q") String q);
}
