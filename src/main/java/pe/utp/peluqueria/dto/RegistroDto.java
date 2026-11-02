package pe.utp.peluqueria.dto;

import jakarta.validation.constraints.*;

public class RegistroDto {
    @NotBlank(message = "Ingresa tus nombres") @Size(max = 80) private String nombres;
    @NotBlank(message = "Ingresa tus apellidos") @Size(max = 80) private String apellidos;
    @NotBlank(message = "Ingresa tu correo") @Email(message = "Correo inválido") @Size(max = 120) private String email;
    @NotBlank(message = "Ingresa tu teléfono") @Pattern(regexp = "\\+?[0-9 ]{7,18}", message = "Teléfono inválido") private String telefono;
    @NotBlank(message = "Ingresa una contraseña") @Size(min = 8, max = 60, message = "Mínimo 8 caracteres") private String password;
    @NotBlank(message = "Confirma tu contraseña") private String confirmPassword;
    @AssertTrue(message = "Debes aceptar los términos y la política de privacidad") private boolean aceptaTerminos;
    private boolean recordatorios = true;

    public String getNombres() { return nombres; }
    public void setNombres(String v) { this.nombres = v; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String v) { this.apellidos = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String v) { this.telefono = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String v) { this.confirmPassword = v; }
    public boolean isAceptaTerminos() { return aceptaTerminos; }
    public void setAceptaTerminos(boolean v) { this.aceptaTerminos = v; }
    public boolean isRecordatorios() { return recordatorios; }
    public void setRecordatorios(boolean v) { this.recordatorios = v; }
}
