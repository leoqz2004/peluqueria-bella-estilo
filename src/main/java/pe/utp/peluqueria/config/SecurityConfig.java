package pe.utp.peluqueria.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    SecurityFilterChain filtros(HttpSecurity http, LoginExitosoHandler loginExitoso) throws Exception {
        http
          .authorizeHttpRequests(a -> a
              // Modo invitado: catalogo, asistente de reserva (solo lectura) y notificaciones son publicos.
              // Confirmar la cita (POST /reservar), Mis citas y las preferencias siguen exigiendo rol CLIENTE.
              .requestMatchers(HttpMethod.GET, "/", "/servicios", "/reservar", "/reservar/horarios", "/notificaciones").permitAll()
              .requestMatchers(HttpMethod.POST, "/reservar/pendiente").permitAll()
              .requestMatchers("/login", "/registro", "/css/**", "/js/**", "/img/**", "/error").permitAll()
              .requestMatchers("/admin/citas/*/confirmar").hasAnyRole("ADMINISTRADOR", "ESTILISTA")
              .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
              .requestMatchers("/agenda/**").hasRole("ESTILISTA")
              .requestMatchers("/citas/**").hasAnyRole("CLIENTE", "ADMINISTRADOR")
              .requestMatchers("/reservar/**", "/mis-citas", "/servicios", "/notificaciones/**").hasRole("CLIENTE")
              .anyRequest().authenticated())
          .formLogin(f -> f.loginPage("/login").successHandler(loginExitoso).permitAll())
          .logout(l -> l.logoutSuccessUrl("/servicios?salir").invalidateHttpSession(true))
          .headers(h -> h.frameOptions(fo -> fo.deny()));
        // CSRF permanece activo (por defecto) para todos los formularios POST.
        return http.build();
    }
}
