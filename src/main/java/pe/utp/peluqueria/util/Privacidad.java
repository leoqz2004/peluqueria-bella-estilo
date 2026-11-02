package pe.utp.peluqueria.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Utilidades de privacidad para el tratamiento seguro de la informacion (Apache Commons Lang3).
 * Se usan antes de escribir datos personales en los registros (Logback).
 */
public final class Privacidad {
    private Privacidad() { }

    /**
     * Enmascara un correo para los logs: {@code valeria.rojas@correo.com} -> {@code v*****@correo.com}.
     *
     * @param email correo a enmascarar (puede ser nulo o vacio)
     * @return correo enmascarado, o {@code "***"} si no tiene formato valido
     */
    public static String enmascararEmail(String email) {
        String correo = StringUtils.trimToEmpty(email);
        int arroba = correo.indexOf('@');
        if (arroba < 1) return "***";
        return correo.charAt(0) + StringUtils.repeat('*', Math.min(arroba - 1, 5)) + correo.substring(arroba);
    }
}
