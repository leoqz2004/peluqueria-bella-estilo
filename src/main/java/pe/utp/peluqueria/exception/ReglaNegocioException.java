package pe.utp.peluqueria.exception;

/** Violacion de una regla de negocio distinta del cruce (estado invalido, servicio no ofrecido, etc.). */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String msg) { super(msg); }
}
