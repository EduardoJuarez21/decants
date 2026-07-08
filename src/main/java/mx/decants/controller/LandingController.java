package mx.decants.controller;

import mx.decants.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingController {

    private final ProductoService productoService;

    public LandingController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("productosDesigner", productoService.activosPorCategoria("alta-perfumeria"));
        model.addAttribute("productosArabe",    productoService.activosPorCategoria("nicho-arabe"));
        return "index";
    }
}
