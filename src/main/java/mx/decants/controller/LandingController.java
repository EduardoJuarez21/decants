package mx.decants.controller;

import mx.decants.entity.Cupon;
import mx.decants.repository.CuponRepository;
import mx.decants.service.ConfiguracionService;
import mx.decants.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingController {

    private final ProductoService productoService;
    private final ConfiguracionService configuracionService;
    private final CuponRepository cuponRepository;

    public LandingController(ProductoService productoService,
                             ConfiguracionService configuracionService,
                             CuponRepository cuponRepository) {
        this.productoService = productoService;
        this.configuracionService = configuracionService;
        this.cuponRepository = cuponRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("productosDesigner",   productoService.activosPorCategoria("alta-perfumeria"));
        model.addAttribute("productosArabe",       productoService.activosPorCategoria("nicho-arabe"));
        model.addAttribute("costoEnvio",           configuracionService.getCostoEnvio());
        model.addAttribute("umbralEnvioGratis",    configuracionService.getUmbralEnvioGratis());
        model.addAttribute("textoEnvioLocal",      configuracionService.getTextoEnvioLocal());
        model.addAttribute("waNumero",             configuracionService.getWhatsappNegocio());

        cuponRepository.findAllByOrderByIdDesc().stream()
            .filter(Cupon::isActivo)
            .findFirst()
            .ifPresent(c -> model.addAttribute("cuponActivo", c));

        return "index";
    }
}