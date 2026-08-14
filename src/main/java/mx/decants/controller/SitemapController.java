package mx.decants.controller;

import mx.decants.service.ProductoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SitemapController {

    private final ProductoService productoService;

    @Value("${app.base-url:https://auradecantsmx.com}")
    private String baseUrl;

    public SitemapController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
          .append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        agregarUrl(sb, baseUrl + "/");
        agregarUrl(sb, baseUrl + "/catalogo");
        agregarUrl(sb, baseUrl + "/terminos");

        productoService.activosTodos().stream()
                .filter(p -> p.getSlug() != null && !p.getSlug().isBlank())
                .forEach(p -> agregarUrl(sb, baseUrl + "/perfume/" + p.getSlug()));

        sb.append("</urlset>\n");
        return sb.toString();
    }

    private void agregarUrl(StringBuilder sb, String loc) {
        sb.append("  <url><loc>").append(loc).append("</loc></url>\n");
    }
}
