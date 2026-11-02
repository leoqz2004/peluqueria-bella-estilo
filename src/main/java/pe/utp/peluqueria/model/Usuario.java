package pe.utp.peluqueria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Usuario base (administrador). Cliente y Estilista lo especializan (herencia JOINED = tablas usuario/cliente/estilista). */
@Entity @Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_usuario")
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "id_rol")
    private Rol rol;
    private String nombres;
    private String apellidos;
    @Column(unique = true, nullable = false)
    private String email;
    private String telefono;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    private boolean activo = true;
    @Column(name = "fecha_registro", insertable = false, updatable = false)
    private LocalDateTime fechaRegistro;
    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    public String getNombreCompleto() { return nombres + " " + apellidos; }
    public String getNombreCorto() { return nombres + " " + apellidos.charAt(0) + "."; }
    public String getIniciales() { return ("" + nombres.charAt(0) + apellidos.charAt(0)).toUpperCase(); }
    public Long getId() { return id; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public String getNombres() { return nombres; }
    public void setNombres(String v) { this.nombres = v; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String v) { this.apellidos = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String v) { this.telefono = v; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String v) { this.passwordHash = v; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean v) { this.activo = v; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime v) { this.ultimoAcceso = v; }
}
