package pe.utp.peluqueria.exception;

/** RN01: la cita se cruza con otra activa del mismo estilista. */
public class CruceHorarioException extends RuntimeException {
    public CruceHorarioException(String msg) { super(msg); }
}
