package mx.decants.controller;

import mx.decants.entity.Cupon;
import mx.decants.entity.Kit;
import mx.decants.repository.CuponRepository;
import mx.decants.repository.KitRepository;
import mx.decants.service.ConfiguracionService;
import mx.decants.service.ProductoService;
import mx.decants.service.VisitaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class LandingController {

    private final ProductoService productoService;
    private final ConfiguracionService configuracionService;
    private final CuponRepository cuponRepository;
    private final KitRepository kitRepository;
    private final VisitaService visitaService;

    public LandingController(ProductoService productoService,
                             ConfiguracionService configuracionService,
                             CuponRepository cuponRepository,
                             KitRepository kitRepository,
                             VisitaService visitaService) {
        this.productoService = productoService;
        this.configuracionService = configuracionService;
        this.cuponRepository = cuponRepository;
        this.kitRepository = kitRepository;
        this.visitaService = visitaService;
    }

    @GetMapping("/")
    public String index(
            @RequestParam(defaultValue = "0")     int    dp,
            @RequestParam(defaultValue = "todos") String df,
            @RequestParam(defaultValue = "0")     int    ap,
            @RequestParam(defaultValue = "todos") String af,
            @RequestParam(value = "ref", required = false) String ref,
            Model model) {

        visitaService.registrar(ref);

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

        Map<String, Kit> kitsMap = kitRepository.findAllByOrderByOrdenAsc().stream()
                .collect(Collectors.toMap(Kit::getSlug, k -> k));
        model.addAttribute("kitsMap", kitsMap);

        cuponRepository.findAllByOrderByIdDesc().stream()
            .filter(Cupon::isActivo)
            .findFirst()
            .ifPresent(c -> model.addAttribute("cuponActivo", c));

        return "index";
    }

    @GetMapping("/catalogo")
    public String catalogo(Model model) {
        model.addAttribute("productos", productoService.activosTodos());
        model.addAttribute("waNumero",  configuracionService.getWhatsappNegocio());
        return "catalogo";
    }

    @GetMapping("/terminos")
    public String terminos() {
        return "terminos";
    }
}
