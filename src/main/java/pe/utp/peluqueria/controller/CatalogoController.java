package pe.utp.peluqueria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.utp.peluqueria.service.CatalogoService;

@Controller
public class CatalogoController {
    private final CatalogoService catalogo;
    public CatalogoController(CatalogoService catalogo) { this.catalogo = catalogo; }

    @GetMapping("/servicios")
    public String servicios(Model m) { m.addAttribute("servicios", catalogo.catalogoActivo()); return "servicios"; }
}
