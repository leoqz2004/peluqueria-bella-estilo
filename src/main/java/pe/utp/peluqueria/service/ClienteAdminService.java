package pe.utp.peluqueria.service;

import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.dto.ClienteResumen;
import pe.utp.peluqueria.model.Cliente;
import pe.utp.peluqueria.repository.CitaRepository;
import pe.utp.peluqueria.repository.ClienteRepository;

@Service
public class ClienteAdminService {
    private final ClienteRepository clientes;
    private final CitaRepository citas;
    public ClienteAdminService(ClienteRepository clientes, CitaRepository citas) { this.clientes = clientes; this.citas = citas; }

    /** P13: clientes con cantidad de citas y ultima visita. */
    @Transactional(readOnly = true)
    public List<ClienteResumen> listar(String q) {
        Map<Long, Object[]> stats = new HashMap<>();
        for (Object[] f : citas.resumenPorCliente()) stats.put((Long) f[0], f);
        List<ClienteResumen> out = new ArrayList<>();
        for (Cliente c : clientes.buscar(q == null ? "" : q.trim())) {
            Object[] f = stats.get(c.getId());
            out.add(new ClienteResumen(c, f == null ? 0 : (Long) f[1], f == null ? null : (LocalDate) f[2]));
        }
        return out;
    }
}
