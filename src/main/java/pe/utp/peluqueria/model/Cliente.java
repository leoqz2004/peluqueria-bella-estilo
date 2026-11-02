package pe.utp.peluqueria.model;

import jakarta.persistence.*;

@Entity @Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Cliente extends Usuario {
    @Column(name = "acepta_whatsapp") private boolean aceptaWhatsapp = true;
    @Column(name = "acepta_correo") private boolean aceptaCorreo = true;
    private String observaciones;

    public boolean isAceptaWhatsapp() { return aceptaWhatsapp; }
    public void setAceptaWhatsapp(boolean v) { this.aceptaWhatsapp = v; }
    public boolean isAceptaCorreo() { return aceptaCorreo; }
    public void setAceptaCorreo(boolean v) { this.aceptaCorreo = v; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String v) { this.observaciones = v; }
}
