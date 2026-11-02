package pe.utp.peluqueria.service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.dto.ReservaDto;
import pe.utp.peluqueria.exception.*;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.*;

@Service
public class CitaService {
    private static final Map<EstadoCita, Set<EstadoCita>> TRANSICIONES = Map.of(
        EstadoCita.PENDIENTE, Set.of(EstadoCita.CONFIRMADA, EstadoCita.CANCELADA),
        EstadoCita.CONFIRMADA, Set.of(EstadoCita.ATENDIDA, EstadoCita.AUSENTE, EstadoCita.CANCELADA),
        EstadoCita.ATENDIDA, Set.of(), EstadoCita.CANCELADA, Set.of(), EstadoCita.AUSENTE, Set.of());

    private final CitaRepository citas;
    private final ClienteRepository clientes;
    private final EstilistaRepository estilistas;
    private final ServicioRepository servicios;
    private final HorarioEstilistaRepository horarios;
    private final NotificacionService notificaciones;
    private final int paso;

    public CitaService(CitaRepository citas, ClienteRepository clientes, EstilistaRepository estilistas,
                       ServicioRepository servicios, HorarioEstilistaRepository horarios,
                       NotificacionService notificaciones, @Value("${app.horarios.paso-minutos}") int paso) {
        this.citas = citas; this.clientes = clientes; this.estilistas = estilistas;
        this.servicios = servicios; this.horarios = horarios; this.notificaciones = notificaciones; this.paso = paso;
    }

    /** RN01, RN02, RN03: crea la cita validando cruce y horario laboral dentro de una sola transaccion. */
    @Transactional
    public Cita crearCita(Long clienteId, ReservaDto dto) {
        Cliente cliente = clientes.findById(clienteId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
        Estilista estilista = estilistas.findById(dto.getEstilistaId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Estilista no encontrado"));
        List<Servicio> seleccion = servicios.findAllById(dto.getServicioIds());
        if (seleccion.size() != new HashSet<>(dto.getServicioIds()).size())
            throw new RecursoNoEncontradoException("Algun servicio no existe");
        for (Servicio s : seleccion) {
            if (!s.isActivo()) throw new ReglaNegocioException("El servicio " + s.getNombre() + " no esta disponible");
            if (!estilista.ofrece(s)) throw new ReglaNegocioException(estilista.getNombreCompleto() + " no ofrece " + s.getNombre());
        }
        int minutos = seleccion.stream().mapToInt(Servicio::getDuracionMin).sum();          // RN02
        LocalTime fin = dto.getHora().plusMinutes(minutos);
        if (fin.isBefore(dto.getHora())) throw new ReglaNegocioException("La cita cruza la medianoche");
        if (LocalDateTime.of(dto.getFecha(), dto.getHora()).isBefore(LocalDateTime.now()))
            throw new ReglaNegocioException("La fecha y hora ya pasaron");
        validarDentroDeHorario(estilista.getId(), dto.getFecha(), dto.getHora(), fin);      // RN03
        if (!citas.buscarCrucesConBloqueo(estilista.getId(), dto.getFecha(), dto.getHora(), fin, null).isEmpty())
            throw new CruceHorarioException("El estilista ya tiene una cita en ese horario");   // RN01

        Cita cita = new Cita();
        cita.setCliente(cliente); cita.setEstilista(estilista); cita.setSede(estilista.getSede());
        cita.setFecha(dto.getFecha()); cita.setHoraInicio(dto.getHora()); cita.setHoraFin(fin);
        BigDecimal total = BigDecimal.ZERO;
        for (Servicio s : seleccion) {
            DetalleCita d = new DetalleCita(cita, s);
            cita.getDetalles().add(d);
            total = total.add(d.getPrecioAplicado());
        }
        cita.setTotal(total);
        citas.save(cita);   // la restriccion UNIQUE de BD es la ultima defensa
        notificaciones.registrar(cita, TipoNotificacion.REGISTRO);
        return cita;
    }

    @Transactional
    public void confirmar(Long id) {
        Cita c = obtener(id);
        cambiarEstado(c, EstadoCita.CONFIRMADA);
        notificaciones.registrar(c, TipoNotificacion.CONFIRMACION);
        notificaciones.programarRecordatorios(c);
    }

    @Transactional
    public void cancelar(Long id, String motivo, Usuario actor) {
        Cita c = obtener(id);
        verificarPropiedad(c, actor);
        cambiarEstado(c, EstadoCita.CANCELADA);
        c.setMotivoCancelacion(motivo);
        notificaciones.anularRecordatoriosPendientes(c);
        notificaciones.registrar(c, TipoNotificacion.CANCELACION);
    }

    @Transactional
    public void reprogramar(Long id, LocalDate fecha, LocalTime hora, Usuario actor) {
        Cita c = obtener(id);
        verificarPropiedad(c, actor);
        if (c.getEstado() != EstadoCita.PENDIENTE && c.getEstado() != EstadoCita.CONFIRMADA)
            throw new ReglaNegocioException("Solo se reprograman citas pendientes o confirmadas");
        int minutos = (int) Duration.between(c.getHoraInicio(), c.getHoraFin()).toMinutes();
        LocalTime fin = hora.plusMinutes(minutos);
        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now()))
            throw new ReglaNegocioException("La fecha y hora ya pasaron");
        validarDentroDeHorario(c.getEstilista().getId(), fecha, hora, fin);
        if (!citas.buscarCrucesConBloqueo(c.getEstilista().getId(), fecha, hora, fin, c.getId()).isEmpty())
            throw new CruceHorarioException("El estilista ya tiene una cita en ese horario");
        c.setFecha(fecha); c.setHoraInicio(hora); c.setHoraFin(fin);
        notificaciones.anularRecordatoriosPendientes(c);
        notificaciones.registrar(c, TipoNotificacion.REPROGRAMACION);
        if (c.getEstado() == EstadoCita.CONFIRMADA) notificaciones.programarRecordatorios(c);
    }

    /** El estilista marca ATENDIDA o AUSENTE solo en sus propias citas. */
    @Transactional
    public void marcarResultado(Long id, EstadoCita resultado, Usuario estilista) {
        if (resultado != EstadoCita.ATENDIDA && resultado != EstadoCita.AUSENTE)
            throw new ReglaNegocioException("Resultado invalido");
        Cita c = obtener(id);
        if (!c.getEstilista().getId().equals(estilista.getId())) throw new AccessDeniedException("Cita ajena");
        cambiarEstado(c, resultado);
    }

    /** RN03: horas de inicio libres (paso configurable) para un estilista, fecha y conjunto de servicios. */
    @Transactional(readOnly = true)
    public List<LocalTime> horariosLibres(Long estilistaId, LocalDate fecha, List<Long> servicioIds) {
        int minutos = servicios.findAllById(servicioIds).stream().mapToInt(Servicio::getDuracionMin).sum();
        return calcularLibres(estilistaId, fecha, minutos, null);
    }

    /** Horas libres para mover una cita existente a otra fecha (ignora la propia cita). */
    @Transactional(readOnly = true)
    public List<LocalTime> horariosParaReprogramar(Long citaId, LocalDate fecha, Usuario actor) {
        Cita c = obtener(citaId);
        verificarPropiedad(c, actor);
        return calcularLibres(c.getEstilista().getId(), fecha, (int) c.getDuracionMin(), c.getId());
    }

    private List<LocalTime> calcularLibres(Long estilistaId, LocalDate fecha, int minutos, Long excluirCita) {
        if (minutos <= 0) return List.of();
        List<Cita> ocupadas = citas.activasDelDia(estilistaId, fecha).stream()
            .filter(o -> excluirCita == null || !excluirCita.equals(o.getId())).toList();
        List<LocalTime> libres = new ArrayList<>();
        for (HorarioEstilista h : horarios.findByEstilistaIdAndDiaSemanaOrderByHoraInicio(estilistaId, fecha.getDayOfWeek().getValue())) {
            for (LocalTime t = h.getHoraInicio(); !t.plusMinutes(minutos).isAfter(h.getHoraFin()); t = t.plusMinutes(paso)) {
                LocalTime ini = t, fin = t.plusMinutes(minutos);
                boolean pasado = LocalDateTime.of(fecha, ini).isBefore(LocalDateTime.now());
                boolean cruza = ocupadas.stream().anyMatch(o -> o.getHoraInicio().isBefore(fin) && o.getHoraFin().isAfter(ini));
                if (!pasado && !cruza) libres.add(ini);
                if (t.plusMinutes(paso).isBefore(t)) break;   // evita vuelta de reloj
            }
        }
        return libres;
    }

    @Transactional(readOnly = true)
    public List<Cita> historial(Long clienteId) { return citas.historialDeCliente(clienteId); }

    @Transactional(readOnly = true)
    public List<Cita> agenda(Long estilistaId, LocalDate fecha) { return citas.agendaDelEstilista(estilistaId, fecha); }

    // ---------- auxiliares ----------
    private Cita obtener(Long id) {
        return citas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));
    }

    private void cambiarEstado(Cita c, EstadoCita nuevo) {                       // RN04
        if (!TRANSICIONES.get(c.getEstado()).contains(nuevo))
            throw new ReglaNegocioException("No se puede pasar de " + c.getEstado() + " a " + nuevo);
        c.setEstado(nuevo);
    }

    private void verificarPropiedad(Cita c, Usuario actor) {
        boolean admin = "ADMINISTRADOR".equals(actor.getRol().getNombre());
        if (!admin && !c.getCliente().getId().equals(actor.getId())) throw new AccessDeniedException("Cita ajena");
    }

    private void validarDentroDeHorario(Long estilistaId, LocalDate fecha, LocalTime ini, LocalTime fin) {
        boolean ok = horarios.findByEstilistaIdAndDiaSemanaOrderByHoraInicio(estilistaId, fecha.getDayOfWeek().getValue())
            .stream().anyMatch(h -> !ini.isBefore(h.getHoraInicio()) && !fin.isAfter(h.getHoraFin()));
        if (!ok) throw new ReglaNegocioException("El horario esta fuera de la jornada del estilista");
    }
}
