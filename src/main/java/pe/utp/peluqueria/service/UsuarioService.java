package pe.utp.peluqueria.service;

import java.time.LocalDateTime;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.dto.RegistroDto;
import pe.utp.peluqueria.exception.*;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.*;

@Service
public class UsuarioService implements UserDetailsService {
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final RolRepository roles;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository u, ClienteRepository c, RolRepository r, PasswordEncoder e) {
        this.usuarios = u; this.clientes = c; this.roles = r; this.encoder = e;
    }

    @Override @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario u = usuarios.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));
        u.setUltimoAcceso(LocalDateTime.now());
        return User.withUsername(u.getEmail()).password(u.getPasswordHash()).disabled(!u.isActivo())
                   .authorities(new SimpleGrantedAuthority("ROLE_" + u.getRol().getNombre())).build();
    }

    @Transactional
    public Cliente registrarCliente(RegistroDto dto) {
        if (usuarios.existsByEmail(dto.getEmail())) throw new ReglaNegocioException("El correo ya esta registrado");
        Cliente c = new Cliente();
        c.setRol(roles.findByNombre("CLIENTE").orElseThrow(() -> new RecursoNoEncontradoException("Rol CLIENTE no existe")));
        c.setNombres(dto.getNombres()); c.setApellidos(dto.getApellidos());
        c.setEmail(dto.getEmail().toLowerCase()); c.setTelefono(dto.getTelefono());
        c.setPasswordHash(encoder.encode(dto.getPassword()));                    // BCrypt
        c.setAceptaWhatsapp(dto.isRecordatorios()); c.setAceptaCorreo(dto.isRecordatorios());
        return clientes.save(c);
    }

    @Transactional
    public void actualizarPreferencias(Long clienteId, boolean whatsapp, boolean correo) {
        Cliente c = clientes.findById(clienteId).orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
        c.setAceptaWhatsapp(whatsapp); c.setAceptaCorreo(correo);
    }

    @Transactional(readOnly = true)
    public Usuario porEmail(String email) {
        return usuarios.findByEmail(email).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
