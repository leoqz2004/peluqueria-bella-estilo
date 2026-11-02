package pe.utp.peluqueria.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.*;

/**
 * Datos de ejemplo SOLO para el perfil "demo" (H2 en memoria). Coinciden con las maquetas del informe APF2.
 * Usuarios de prueba:
 *   recepcion@bellaestilo.pe  / Admin123!    (Administrador - recepcion)
 *   milagros@bellaestilo.pe   / Estilista123! (Estilista; tambien rosa@... y jimena@...)
 *   valeria.rojas@correo.com  / Cliente123!   (Cliente)
 */
@Component @Profile("demo") @Order(1)
public class DemoDataSeeder implements CommandLineRunner {
    private final RolRepository roles; private final SedeRepository sedes; private final CategoriaServicioRepository categorias;
    private final ServicioRepository servicios; private final UsuarioRepository usuarios; private final ClienteRepository clientes;
    private final EstilistaRepository estilistas; private final HorarioEstilistaRepository horarios; private final CitaRepository citas;
    private final PasswordEncoder encoder;

    public DemoDataSeeder(RolRepository roles, SedeRepository sedes, CategoriaServicioRepository categorias, ServicioRepository servicios,
                          UsuarioRepository usuarios, ClienteRepository clientes, EstilistaRepository estilistas,
                          HorarioEstilistaRepository horarios, CitaRepository citas, PasswordEncoder encoder) {
        this.roles = roles; this.sedes = sedes; this.categorias = categorias; this.servicios = servicios; this.usuarios = usuarios;
        this.clientes = clientes; this.estilistas = estilistas; this.horarios = horarios; this.citas = citas; this.encoder = encoder;
    }

    @Override @Transactional
    public void run(String... args) {
        if (roles.count() > 0) return;
        Rol rCliente = rol("CLIENTE"), rEstilista = rol("ESTILISTA"), rAdmin = rol("ADMINISTRADOR");

        Sede sede = new Sede(); sede.setNombre("Bella Estilo — Sede principal"); sede.setDireccion("Av. Larco 245"); sedes.save(sede);

        CategoriaServicio corte = cat("Corte"), color = cat("Color"), peinado = cat("Peinado"), trat = cat("Tratamiento");
        Servicio sCorte = serv(corte, "Corte de cabello", "Estilista a elección", "35.00", 40, true);
        Servicio sTinte = serv(color, "Tinte y color", "Incluye lavado", "120.00", 90, true);
        Servicio sPeinado = serv(peinado, "Peinado para evento", "Reservar con 24 h de anticipación", "80.00", 60, true);
        Servicio sTrat = serv(trat, "Tratamiento capilar", "Hidratación profunda", "65.00", 50, true);
        Servicio sAlisado = serv(trat, "Alisado permanente", "Consultar disponibilidad", "180.00", 120, false);
        Servicio sLavado = serv(peinado, "Lavado y secado", "Incluye masaje capilar", "25.00", 30, true);

        Usuario admin = new Usuario();
        admin.setRol(rAdmin); admin.setNombres("Recepción"); admin.setApellidos("Bella Estilo");
        admin.setEmail("recepcion@bellaestilo.pe"); admin.setTelefono("987000111"); admin.setPasswordHash(encoder.encode("Admin123!"));
        usuarios.save(admin);

        Estilista milagros = estilista(rEstilista, sede, "Milagros", "Ramos", "milagros@bellaestilo.pe", "Corte", sCorte, sLavado, sTrat);
        Estilista rosa = estilista(rEstilista, sede, "Rosa", "Salinas", "rosa@bellaestilo.pe", "Color y tratamientos", sTinte, sTrat, sAlisado, sLavado);
        Estilista jimena = estilista(rEstilista, sede, "Jimena", "Tello", "jimena@bellaestilo.pe", "Peinados de evento", sPeinado, sLavado, sCorte);

        Cliente valeria = cliente(rCliente, "Valeria", "Rojas Díaz", "valeria.rojas@correo.com", "+51 987 654 321");
        Cliente jTorres = cliente(rCliente, "Jimena", "Torres", "jimena.torres@correo.com", "+51 991 223 344");
        Cliente rSanchez = cliente(rCliente, "Rosa", "Sánchez", "rosa.sanchez@correo.com", "+51 977 112 233");
        Cliente cMedina = cliente(rCliente, "Carla", "Medina", "carla.medina@correo.com", "+51 966 554 433");

        LocalDate hoy = LocalDate.now();
        cita(rSanchez, jimena, hoy, "09:30", EstadoCita.ATENDIDA, sLavado);
        cita(jTorres, rosa, hoy, "11:00", EstadoCita.CONFIRMADA, sTinte);
        cita(valeria, milagros, hoy, "15:00", EstadoCita.PENDIENTE, sCorte);
        cita(rSanchez, milagros, hoy, "16:30", EstadoCita.CONFIRMADA, sTrat);
        cita(cMedina, jimena, hoy, "18:00", EstadoCita.CANCELADA, sPeinado);
        cita(valeria, rosa, hoy.plusDays(5), "11:00", EstadoCita.PENDIENTE, sTinte);
        cita(valeria, milagros, hoy.plusDays(8), "16:30", EstadoCita.CONFIRMADA, sTrat);
        cita(valeria, milagros, hoy.minusDays(10), "10:00", EstadoCita.ATENDIDA, sCorte);
        cita(valeria, jimena, hoy.minusDays(20), "15:00", EstadoCita.ATENDIDA, sPeinado);
        cita(jTorres, rosa, hoy.minusDays(12), "09:00", EstadoCita.ATENDIDA, sTinte);
        cita(rSanchez, milagros, hoy.minusDays(6), "12:00", EstadoCita.AUSENTE, sCorte);
        cita(cMedina, rosa, hoy.minusDays(3), "14:00", EstadoCita.CANCELADA, sTrat);
    }

    private Rol rol(String n) { Rol r = new Rol(); r.setNombre(n); return roles.save(r); }
    private CategoriaServicio cat(String n) { CategoriaServicio c = new CategoriaServicio(); c.setNombre(n); return categorias.save(c); }
    private Servicio serv(CategoriaServicio c, String n, String d, String precio, int min, boolean activo) {
        Servicio s = new Servicio(); s.setCategoria(c); s.setNombre(n); s.setDescripcion(d);
        s.setPrecio(new BigDecimal(precio)); s.setDuracionMin(min); s.setActivo(activo); return servicios.save(s);
    }
    private Estilista estilista(Rol rol, Sede sede, String nom, String ape, String email, String esp, Servicio... ofrece) {
        Estilista e = new Estilista();
        e.setRol(rol); e.setNombres(nom); e.setApellidos(ape); e.setEmail(email); e.setTelefono("987000222");
        e.setPasswordHash(encoder.encode("Estilista123!")); e.setSede(sede); e.setEspecialidad(esp);
        for (Servicio s : ofrece) e.getServicios().add(s);
        estilistas.save(e);
        for (int dia = 1; dia <= 6; dia++)   // lunes a sabado; sabado hasta las 17:00
            horarios.save(new HorarioEstilista(e, dia, LocalTime.of(9, 0), dia == 6 ? LocalTime.of(17, 0) : LocalTime.of(19, 0)));
        return e;
    }
    private Cliente cliente(Rol rol, String nom, String ape, String email, String tel) {
        Cliente c = new Cliente();
        c.setRol(rol); c.setNombres(nom); c.setApellidos(ape); c.setEmail(email); c.setTelefono(tel);
        c.setPasswordHash(encoder.encode("Cliente123!")); return clientes.save(c);
    }
    private void cita(Cliente cli, Estilista est, LocalDate fecha, String hora, EstadoCita estado, Servicio... ss) {
        Cita c = new Cita();
        c.setCliente(cli); c.setEstilista(est); c.setSede(est.getSede()); c.setFecha(fecha);
        LocalTime ini = LocalTime.parse(hora); int min = 0; BigDecimal total = BigDecimal.ZERO;
        for (Servicio s : ss) { c.getDetalles().add(new DetalleCita(c, s)); min += s.getDuracionMin(); total = total.add(s.getPrecio()); }
        c.setHoraInicio(ini); c.setHoraFin(ini.plusMinutes(min)); c.setTotal(total); c.setEstado(estado);
        citas.save(c);
    }
}
