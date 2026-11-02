package pe.utp.peluqueria.config;

import org.springframework.context.annotation.*;
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
    SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
          .authorizeHttpRequests(a -> a
              .requestMatchers("/login", "/registro", "/css/**", "/js/**", "/img/**", "/error").permitAll()
              .requestMatchers("/admin/citas/*/confirmar").hasAnyRole("ADMINISTRADOR", "ESTILISTA")
              .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
              .requestMatchers("/agenda/**").hasRole("ESTILISTA")
              .requestMatchers("/citas/**").hasAnyRole("CLIENTE", "ADMINISTRADOR")
              .requestMatchers("/reservar/**", "/mis-citas", "/servicios", "/notificaciones/**").hasRole("CLIENTE")
              .anyRequest().authenticated())
          .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/inicio", true).permitAll())
          .logout(l -> l.logoutSuccessUrl("/login?salir"))
          .headers(h -> h.frameOptions(fo -> fo.deny()));
        // CSRF permanece activo (por defecto) para todos los formularios POST.
        return http.build();
    }
}
