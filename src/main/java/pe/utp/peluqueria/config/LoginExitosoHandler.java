package pe.utp.peluqueria.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pe.utp.peluqueria.service.ReservaPendienteService;
import pe.utp.peluqueria.util.Acceso;
import pe.utp.peluqueria.util.Privacidad;

/**
 * Decide a donde ir despues de iniciar sesion.
 * <ul>
 *   <li>CLIENTE con reserva pendiente vigente: vuelve al paso de confirmacion ({@code /reservar?retomar}).</li>
 *   <li>Cualquier otro caso: {@code /inicio}, que enruta a Administrador, Estilista o Cliente.</li>
 *   <li>Administrador o Estilista: la reserva pendiente se descarta (solo aplica a clientes).</li>
 * </ul>
 */
@Component
public class LoginExitosoHandler implements AuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(LoginExitosoHandler.class);
    private final ReservaPendienteService pendientes;

    public LoginExitosoHandler(ReservaPendienteService pendientes) { this.pendientes = pendientes; }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication auth) throws IOException {
        HttpSession sesion = request.getSession(false);
        String destino = "/inicio";
        if (Acceso.esCliente(auth) && pendientes.obtenerVigente(sesion).isPresent()) {
            destino = "/reservar?retomar";
        } else {
            pendientes.descartar(sesion);
        }
        log.info("Inicio de sesion correcto: {} -> {}", Privacidad.enmascararEmail(auth.getName()), destino);
        response.sendRedirect(request.getContextPath() + destino);
    }
}
