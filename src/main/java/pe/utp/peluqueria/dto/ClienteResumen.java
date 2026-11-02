package pe.utp.peluqueria.dto;

import java.time.LocalDate;
import pe.utp.peluqueria.model.Cliente;

public class ClienteResumen {
    private final Cliente cliente; private final long citas; private final LocalDate ultimaVisita;
    public ClienteResumen(Cliente cliente, long citas, LocalDate ultimaVisita) {
        this.cliente = cliente; this.citas = citas; this.ultimaVisita = ultimaVisita;
    }
    public Cliente getCliente() { return cliente; }
    public long getCitas() { return citas; }
    public LocalDate getUltimaVisita() { return ultimaVisita; }
}
