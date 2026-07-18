package mx.decants.controller;

import mx.decants.service.ResenaService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    @PostMapping("/resena")
    public String enviarResena(
            @RequestParam String nombre,
            @RequestParam String texto,
            @RequestParam int calificacion,
            @RequestParam(name = "telefono", required = false) String honeypot,
            RedirectAttributes ra) {

        // Honeypot: bots fill this hidden field, usuarios reales no
        if (honeypot != null && !honeypot.isBlank()) {
            return "redirect:/#resenas";
        }

        String nombreTrim = nombre == null ? "" : nombre.trim();
        String textoTrim  = texto  == null ? "" : texto.trim();

        if (nombreTrim.isBlank() || textoTrim.isBlank() || calificacion < 1 || calificacion > 5) {
            ra.addFlashAttribute("resenaError", "Por favor completa todos los campos.");
            return "redirect:/#resenas";
        }
        if (nombreTrim.length() > 100 || textoTrim.length() > 1000) {
            ra.addFlashAttribute("resenaError", "El texto excede el límite permitido.");
            return "redirect:/#resenas";
        }

        resenaService.guardar(nombreTrim, textoTrim, calificacion);
        ra.addFlashAttribute("resenaOk", true);
        return "redirect:/#resenas";
    }
}
