package pe.utp.peluqueria.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.dto.ServicioDto;
import pe.utp.peluqueria.exception.*;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.*;

/** Mantenimientos del administrador: servicios y estilistas. */
@Service
public class CatalogoService {
    private final ServicioRepository servicios;
    private final CategoriaServicioRepository categorias;
    private final EstilistaRepository estilistas;
    private final SedeRepository sedes;
    private final RolRepository roles;
    private final UsuarioRepository usuarios;
    private final HorarioEstilistaRepository horarios;
    private final PasswordEncoder encoder;

    public CatalogoService(ServicioRepository s, CategoriaServicioRepository c, EstilistaRepository e, SedeRepository sd,
                           RolRepository r, UsuarioRepository u, HorarioEstilistaRepository h, PasswordEncoder enc) {
        servicios = s; categorias = c; estilistas = e; sedes = sd; roles = r; usuarios = u; horarios = h; encoder = enc;
    }

    @Transactional(readOnly = true) public List<Servicio> catalogoActivo() { return servicios.findByActivoTrueOrderByCategoriaNombreAscNombreAsc(); }
    @Transactional(readOnly = true) public List<Servicio> todosLosServicios() { return servicios.findAll(); }
    @Transactional(readOnly = true) public List<CategoriaServicio> categorias() { return categorias.findAll(); }
    @Transactional(readOnly = true)
    public List<Estilista> estilistasActivos() {
        List<Estilista> lista = estilistas.findByActivoTrue();
        lista.forEach(e -> e.getServicios().size());     // inicializa la coleccion perezosa para la vista
        return lista;
    }

    @Transactional(readOnly = true)
    public List<HorarioEstilista> horariosDe(Long estilistaId) { return horarios.findByEstilistaIdOrderByDiaSemana(estilistaId); }

    @Transactional
    public void actualizarServicio(Long id, ServicioDto dto) {
        Servicio s = servicios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Servicio"));
        s.setNombre(dto.getNombre()); s.setDescripcion(dto.getDescripcion());
        s.setPrecio(dto.getPrecio()); s.setDuracionMin(dto.getDuracionMin());
        s.setCategoria(categorias.findById(dto.getCategoriaId()).orElseThrow(() -> new RecursoNoEncontradoException("Categoria")));
    }

    @Transactional
    public Servicio crearServicio(ServicioDto dto) {
        Servicio s = new Servicio();
        s.setNombre(dto.getNombre()); s.setDescripcion(dto.getDescripcion());
        s.setPrecio(dto.getPrecio()); s.setDuracionMin(dto.getDuracionMin());
        s.setCategoria(categorias.findById(dto.getCategoriaId()).orElseThrow(() -> new RecursoNoEncontradoException("Categoria")));
        return servicios.save(s);
    }

    @Transactional
    public void cambiarEstadoServicio(Long id, boolean activo) {
        servicios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Servicio")).setActivo(activo);
    }

    /** Alta de estilista con su jornada (dias 1-7) y los servicios que ofrece. */
    @Transactional
    public Estilista crearEstilista(String nombres, String apellidos, String email, String telefono, String claveInicial,
                                    String especialidad, List<Long> servicioIds, Set<Integer> dias, LocalTime ini, LocalTime fin) {
        if (usuarios.existsByEmail(email)) throw new ReglaNegocioException("El correo ya esta registrado");
        if (!fin.isAfter(ini)) throw new ReglaNegocioException("La hora fin debe ser mayor que la hora inicio");
        Estilista e = new Estilista();
        e.setRol(roles.findByNombre("ESTILISTA").orElseThrow(() -> new RecursoNoEncontradoException("Rol ESTILISTA")));
        e.setNombres(nombres); e.setApellidos(apellidos); e.setEmail(email.toLowerCase()); e.setTelefono(telefono);
        e.setPasswordHash(encoder.encode(claveInicial));
        e.setEspecialidad(especialidad);
        e.setSede(sedes.findAll().stream().findFirst().orElseThrow(() -> new RecursoNoEncontradoException("Sede")));
        e.getServicios().addAll(servicios.findAllById(servicioIds));
        estilistas.save(e);
        for (int d : dias) horarios.save(new HorarioEstilista(e, d, ini, fin));
        return e;
    }
}
