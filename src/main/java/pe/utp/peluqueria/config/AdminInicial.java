package pe.utp.peluqueria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.utp.peluqueria.model.Usuario;
import pe.utp.peluqueria.repository.RolRepository;
import pe.utp.peluqueria.repository.UsuarioRepository;

/** Crea el primer administrador si se definen ADMIN_EMAIL y ADMIN_PASSWORD (variables de entorno). */
@Configuration
public class AdminInicial {
    @Bean @Order(2)
    CommandLineRunner crearAdmin(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder,
                                 @Value("${ADMIN_EMAIL:}") String email, @Value("${ADMIN_PASSWORD:}") String clave) {
        return args -> {
            if (email.isBlank() || clave.isBlank() || usuarios.existsByEmail(email)) return;
            Usuario u = new Usuario();
            u.setRol(roles.findByNombre("ADMINISTRADOR").orElseThrow());
            u.setNombres("Administrador"); u.setApellidos("Sistema");
            u.setEmail(email.toLowerCase()); u.setTelefono("000000000");
            u.setPasswordHash(encoder.encode(clave));
            usuarios.save(u);
        };
    }
}
