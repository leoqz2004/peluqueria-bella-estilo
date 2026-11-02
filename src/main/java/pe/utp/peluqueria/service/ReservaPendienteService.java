package pe.utp.peluqueria.service;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import pe.utp.peluqueria.dto.ReservaDto;
import pe.utp.peluqueria.dto.ReservaPendiente;

/**
 * Contrato para conservar la reserva elegida por un invitado mientras inicia sesion.
 * Los controladores y el manejador de login dependen de esta abstraccion (inversion de dependencias, SOLID).
 */
public interface ReservaPendienteService {

    /** Guarda (reemplazando la anterior) la seleccion del invitado en su sesion. */
    void guardar(HttpSession sesion, ReservaDto dto);

    /** @return la reserva guardada si existe y no ha caducado; si caduco la elimina y devuelve vacio. */
    Optional<ReservaPendiente> obtenerVigente(HttpSession sesion);

    /** Elimina la reserva guardada (al confirmar, al caducar o si el usuario no es cliente). */
    void descartar(HttpSession sesion);
}
