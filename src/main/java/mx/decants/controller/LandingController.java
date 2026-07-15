package mx.decants.controller;

import mx.decants.entity.Cupon;
import mx.decants.repository.CuponRepository;
import mx.decants.service.ConfiguracionService;
import mx.decants.service.ProductoService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String index(
            @RequestParam(defaultValue = "0")     int    dp,
            @RequestParam(defaultValue = "todos") String df,
            @RequestParam(defaultValue = "0")     int    ap,
            @RequestParam(defaultValue = "todos") String af,
            Model model) {

        var designer = productoService.activosPorCategoriaPaginados("alta-perfumeria", df, dp);
        var arabe    = productoService.activosPorCategoriaPaginados("nicho-arabe",    af, ap);

        model.addAttribute("productosDesigner", designer.getContent());
        model.addAttribute("designerPage",      designer);
        model.addAttribute("dp", dp);
        model.addAttribute("df", df);

        model.addAttribute("productosArabe", arabe.getContent());
        model.addAttribute("arabePage",      arabe);
        model.addAttribute("ap", ap);
        model.addAttribute("af", af);

        model.addAttribute("costoEnvio",        configuracionService.getCostoEnvio());
        model.addAttribute("umbralEnvioGratis", configuracionService.getUmbralEnvioGratis());
        model.addAttribute("textoEnvioLocal",   configuracionService.getTextoEnvioLocal());
        model.addAttribute("waNumero",          configuracionService.getWhatsappNegocio());

        cuponRepository.findAllByOrderByIdDesc().stream()
            .filter(Cupon::isActivo)
            .findFirst()
            .ifPresent(c -> model.addAttribute("cuponActivo", c));

        return "index";
    }

    @GetMapping("/terminos")
    public String terminos() {
        return "terminos";
    }
}
