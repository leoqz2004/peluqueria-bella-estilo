package pe.utp.peluqueria.controller;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.utp.peluqueria.exception.*;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({CruceHorarioException.class, ReglaNegocioException.class})
    public String reglaNegocio(RuntimeException ex, Model model) {
        model.addAttribute("mensaje", ex.getMessage());
        return "error-negocio";
    }
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    public String noEncontrado(RecursoNoEncontradoException ex, Model model) {
        model.addAttribute("mensaje", ex.getMessage());
        return "error-negocio";
    }
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.FORBIDDEN)
    public String prohibido(Model model) {
        model.addAttribute("mensaje", "No tienes permiso para esta operacion");
        return "error-negocio";
    }
}
