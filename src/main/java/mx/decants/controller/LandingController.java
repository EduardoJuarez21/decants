package mx.decants.controller;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.servlet.http.HttpServletResponse;
import mx.decants.entity.Cupon;
import mx.decants.entity.Kit;
import mx.decants.repository.CuponRepository;
import mx.decants.repository.KitRepository;
import mx.decants.service.ConfiguracionService;
import mx.decants.service.ProductoService;
import mx.decants.service.ResenaService;
import mx.decants.service.VisitaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Controller
public class LandingController {

    private final ProductoService productoService;
    private final ConfiguracionService configuracionService;
    private final CuponRepository cuponRepository;
    private final KitRepository kitRepository;
    private final VisitaService visitaService;
    private final ResenaService resenaService;
    private final TemplateEngine templateEngine;

    @Value("${app.uploads-dir:/app/uploads}")
    private String uploadsDir;

    private static final Map<String, String> IMAGE_CACHE = new ConcurrentHashMap<>();

    public LandingController(ProductoService productoService,
                             ConfiguracionService configuracionService,
                             CuponRepository cuponRepository,
                             KitRepository kitRepository,
                             VisitaService visitaService,
                             ResenaService resenaService,
                             TemplateEngine templateEngine) {
        this.productoService = productoService;
        this.configuracionService = configuracionService;
        this.cuponRepository = cuponRepository;
        this.kitRepository = kitRepository;
        this.visitaService = visitaService;
        this.resenaService = resenaService;
        this.templateEngine = templateEngine;
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
        model.addAttribute("resenas",           resenaService.listarAprobadas());

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
        var productos = productoService.activosTodos();
        model.addAttribute("productos", productos);
        model.addAttribute("waNumero",   configuracionService.getWhatsappNegocio());
        model.addAttribute("promoTexto", configuracionService.getPromoTexto());

        LocalDate hoy = LocalDate.now();
        model.addAttribute("mesActual",  mesCapitalizado(hoy));
        model.addAttribute("anioActual", hoy.getYear());

        var tags = productos.stream()
                .filter(p -> p.getCaracteristicas() != null && !p.getCaracteristicas().isBlank())
                .flatMap(p -> java.util.Arrays.stream(p.getCaracteristicas().split("·")))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .distinct()
                .sorted()
                .toList();
        model.addAttribute("tagsDisponibles", tags);

        return "catalogo";
    }

    @GetMapping("/catalogo/descargar")
    public void descargarCatalogo(HttpServletResponse response) throws IOException {
        var productos = productoService.activosTodos();
        LocalDate hoy = LocalDate.now();
        String mes = mesCapitalizado(hoy);
        int anio = hoy.getYear();

        Map<String, Kit> kitsMap = kitRepository.findAllByOrderByOrdenAsc().stream()
                .collect(Collectors.toMap(Kit::getSlug, k -> k));

        Context context = new Context(new Locale("es", "MX"));
        context.setVariable("kitsMap", kitsMap);
        context.setVariable("productos", productos);
        context.setVariable("productosPromo", productos.stream()
                .filter(p -> p.isPromoActivo() && p.getDescuentoPorcentaje() != null)
                .toList());
        context.setVariable("waNumero", configuracionService.getWhatsappNegocio());
        context.setVariable("promoTexto", configuracionService.getPromoTexto());
        context.setVariable("mesActual", mes);
        context.setVariable("anioActual", anio);
        context.setVariable("logoSrc", logoDataUri());
        context.setVariable("imageSrcById", productImageDataUris(productos));

        String html = templateEngine.process("catalogo-pdf", context);

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(pdf);
        try {
            builder.run();
        } catch (Exception e) {
            throw new IOException("No se pudo generar el catalogo PDF", e);
        }

        String filename = "catalogo-aura-decants-" + slug(mes) + "-" + anio + ".pdf";
        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        response.setContentLength(pdf.size());
        pdf.writeTo(response.getOutputStream());
    }

    @GetMapping("/terminos")
    public String terminos() {
        return "terminos";
    }

    private static String mesCapitalizado(LocalDate fecha) {
        String mes = fecha.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "MX"));
        return Character.toUpperCase(mes.charAt(0)) + mes.substring(1);
    }

    private static String slug(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    private static String logoDataUri() throws IOException {
        byte[] logo;
        try (var inputStream = new ClassPathResource("static/auradecantslogo.png").getInputStream()) {
            logo = inputStream.readAllBytes();
        }
        return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(logo);
    }

    private Map<Long, String> productImageDataUris(Iterable<mx.decants.entity.Producto> productos) {
        Map<Long, String> images = new HashMap<>();
        for (var producto : productos) {
            if (producto.getId() == null || producto.getImagenPrincipal() == null || producto.getImagenPrincipal().isBlank()) {
                continue;
            }
            productImageDataUri(producto.getImagenPrincipal()).ifPresent(src -> images.put(producto.getId(), src));
        }
        return images;
    }

    private java.util.Optional<String> productImageDataUri(String imagePath) {
        String path = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
        if (!path.startsWith("img/")) {
            return java.util.Optional.empty();
        }

        String cached = IMAGE_CACHE.get(path);
        if (cached != null) {
            return java.util.Optional.of(cached);
        }

        try (var inputStream = abrirImagen(path)) {
            if (inputStream == null) {
                return java.util.Optional.empty();
            }
            BufferedImage original = ImageIO.read(inputStream);
            if (original == null) {
                return java.util.Optional.empty();
            }

            BufferedImage scaled = scaleToWidth(original, 400);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(scaled, "png", output);
            String dataUri = "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(output.toByteArray());
            IMAGE_CACHE.put(path, dataUri);
            return java.util.Optional.of(dataUri);
        } catch (IOException e) {
            return java.util.Optional.empty();
        }
    }

    private InputStream abrirImagen(String path) {
        var enDisco = Paths.get(uploadsDir).resolve(path);
        if (Files.exists(enDisco)) {
            try {
                return Files.newInputStream(enDisco);
            } catch (IOException e) {
                return null;
            }
        }
        try {
            return new ClassPathResource("static/" + path).getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    private static BufferedImage scaleToWidth(BufferedImage original, int targetWidth) {
        if (original.getWidth() <= targetWidth) {
            return original;
        }

        int targetHeight = Math.max(1, original.getHeight() * targetWidth / original.getWidth());
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return scaled;
    }
}
