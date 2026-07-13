package mx.decants.controller;

import mx.decants.service.ConfiguracionService;
import mx.decants.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingController {

    private final ProductoService productoService;
    private final ConfiguracionService configuracionService;

    public LandingController(ProductoService productoService, ConfiguracionService configuracionService) {
        this.productoService = productoService;
        this.configuracionService = configuracionService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("productosDesigner",   productoService.activosPorCategoria("alta-perfumeria"));
        model.addAttribute("productosArabe",       productoService.activosPorCategoria("nicho-arabe"));
        model.addAttribute("costoEnvio",           configuracionService.getCostoEnvio());
        model.addAttribute("umbralEnvioGratis",    configuracionService.getUmbralEnvioGratis());
        model.addAttribute("textoEnvioLocal",      configuracionService.getTextoEnvioLocal());
        model.addAttribute("waNumero",             configuracionService.getWhatsappNegocio());
        return "index";
    }
}