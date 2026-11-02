package pe.utp.peluqueria.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.utp.peluqueria.service.CatalogoService;
import pe.utp.peluqueria.util.Acceso;

@Controller
public class CatalogoController {
    private final CatalogoService catalogo;
    public CatalogoController(CatalogoService catalogo) { this.catalogo = catalogo; }

    /** P03: catalogo publico (invitado o cliente). Administrador y estilista vuelven a su ventana. */
    @GetMapping("/servicios")
    public String servicios(Model m, Authentication auth) {
        if (Acceso.autenticado(auth) && !Acceso.esCliente(auth)) return "redirect:/inicio";
        m.addAttribute("servicios", catalogo.catalogoActivo());
        return "servicios";
    }
}
