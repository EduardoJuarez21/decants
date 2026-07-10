package mx.decants;

import mx.decants.entity.Producto;
import mx.decants.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductoRepository repo;

    public DataInitializer(ProductoRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.existsByCategoria("alta-perfumeria")) return;

        // ── Alta Perfumería ──────────────────────────────────────────────────

        repo.save(p("Acqua di Giò", "Giorgio Armani", "alta-perfumeria", "hombre",
                "Acuático Aromático · Hombre", "Bergamota · Neroli · Mar · Pachulí",
                "/img/alta-perfumeria/hombre/aqua-di-gio.webp",
                "/img/alta-perfumeria/hombre/car-aqua-di-gio.webp",
                "pi-acqua", 310, 160, 4.9, false, false, 1));

        repo.save(p("Light Blue", "Dolce & Gabbana", "alta-perfumeria", "mujer",
                "Floral Frutal · Mujer", "Manzana · Cedro · Bambú · Ámbar",
                "/img/alta-perfumeria/hombre/dg-light-blue.webp", null,
                "pi-lightblue", 160, null, 4.8, false, false, 2));

        repo.save(p("Ombré Leather", "Tom Ford", "alta-perfumeria", "hombre",
                "Cuero Amaderado · Hombre", "Cuero · Cardamomo · Jazmín · Patchouli",
                "/img/alta-perfumeria/hombre/tom-ford-ombre-leather.webp", null,
                "pi-tomford", 290, null, 4.9, false, false, 3));

        repo.save(p("Donna Born in Roma", "Valentino", "alta-perfumeria", "mujer",
                "Floral Gourmand · Mujer", "Jazmín · Vainilla · Vetiver · Iris",
                "/img/alta-perfumeria/hombre/valentino.webp", null,
                "pi-valentino", 220, null, 4.7, false, false, 4));

        repo.save(p("Cloud", "Ariana Grande", "alta-perfumeria", "mujer",
                "Floral Frutal · Mujer", "Lavanda · Pera · Coco · Praliné",
                "/img/alta-perfumeria/mujer/ariana-cloud.webp", null,
                "pi-cloud", 219, null, 4.5, true, true, 5));

        repo.save(p("Bleu de Chanel", "Chanel", "alta-perfumeria", "hombre",
                "Aromático Amaderado · Hombre", "Limón · Jengibre · Sándalo · Cedro",
                "/img/alta-perfumeria/hombre/blue-de-chanel.webp",
                "/img/alta-perfumeria/hombre/car-blue-de-chanel.webp",
                "pi-bleu", 250, null, 4.9, false, false, 6));

        repo.save(p("Sauvage", "Dior", "alta-perfumeria", "hombre",
                "Aromático Especiado · Hombre", "Bergamota · Pimienta · Ambroxan · Vetiver",
                "/img/alta-perfumeria/hombre/dior-sauvage.webp", null,
                "pi-sauvage", 240, null, 4.9, false, false, 7));

        repo.save(p("Le Male", "Jean Paul Gaultier", "alta-perfumeria", "hombre",
                "Oriental Fougère · Hombre", "Lavanda · Menta · Vainilla · Ámbar",
                "/img/alta-perfumeria/hombre/paul-gaultier.webp",
                "/img/alta-perfumeria/hombre/car-paul-gaultier.webp",
                "pi-lemale", 190, null, 4.8, false, false, 8));

        repo.save(p("1 Million", "Paco Rabanne", "alta-perfumeria", "hombre",
                "Oriental Especiado · Hombre", "Mandarina · Canela · Cuero · Pachulí",
                "/img/alta-perfumeria/hombre/one-million.webp", null,
                "pi-million", 190, null, 4.8, false, false, 9));

        repo.save(p("Black Opium", "Yves Saint Laurent", "alta-perfumeria", "mujer",
                "Oriental Gourmand · Mujer", "Café · Vainilla · Flor Blanca · Cedro",
                "/img/alta-perfumeria/mujer/black-opium.webp", null,
                "pi-blackopium", 200, null, 4.8, false, false, 10));

        repo.save(p("Coco Mademoiselle", "Chanel", "alta-perfumeria", "mujer",
                "Oriental Floral · Mujer", "Naranja · Rosa · Jazmín · Pachulí",
                "/img/alta-perfumeria/mujer/coco-chanel.webp", null,
                "pi-coco", 280, null, 4.9, false, false, 11));

        repo.save(p("Daisy", "Marc Jacobs", "alta-perfumeria", "mujer",
                "Floral Frutal · Mujer", "Fresa · Violeta · Jazmín · Sándalo",
                "/img/alta-perfumeria/mujer/daysi.jfif", null,
                "pi-daisy", 180, null, 4.7, false, false, 12));

        repo.save(p("Good Girl", "Carolina Herrera", "alta-perfumeria", "mujer",
                "Oriental Floral · Mujer", "Jazmín · Cacao · Tonka · Café",
                "/img/alta-perfumeria/mujer/goodgirlcherrera.jfif", null,
                "pi-goodgirl", 220, null, 4.8, false, false, 13));

        repo.save(p("J'adore", "Dior", "alta-perfumeria", "mujer",
                "Floral Afrutado · Mujer", "Ylang Ylang · Rosa · Jazmín · Vainilla",
                "/img/alta-perfumeria/mujer/jadore.webp", null,
                "pi-jadore", 240, null, 4.8, false, false, 14));

        repo.save(p("Libre", "Yves Saint Laurent", "alta-perfumeria", "mujer",
                "Floral Amaderado · Mujer", "Lavanda · Naranja · Jazmín · Musgo",
                "/img/alta-perfumeria/mujer/libre.webp", null,
                "pi-libre", 220, null, 4.8, false, false, 15));

        repo.save(p("Sì", "Giorgio Armani", "alta-perfumeria", "mujer",
                "Chypre Floral · Mujer", "Grosella negra · Rosa · Pachulí · Vainilla",
                "/img/alta-perfumeria/mujer/si-giorgio-armani.webp", null,
                "pi-si", 200, null, 4.7, false, false, 16));

        repo.save(p("Flowerbomb", "Viktor & Rolf", "alta-perfumeria", "mujer",
                "Floral Oriental · Mujer", "Bergamota · Jazmín · Rosa · Pachulí",
                "/img/alta-perfumeria/mujer/viktor.webp", null,
                "pi-flowerbomb", 230, null, 4.8, false, false, 17));

        // ── Nicho Árabe ──────────────────────────────────────────────────────

        repo.save(p("Club de Nuit Intense Man", "Armaf", "nicho-arabe", "hombre",
                "Woody Especiado · Hombre", "Limón · Piña · Abedul · Almizclé",
                "/img/arabe/club-de-nuit.webp", "/img/arabe/car-club-de-nuit.webp",
                "pi-cdni", 99, null, 4.8, true, false, 1));

        repo.save(p("9PM", "Afnan", "nicho-arabe", "hombre",
                "Oriental Vainilla · Hombre", "Manzana · Canela · Vainilla · Ámbar",
                "/img/arabe/9pm.webp", "/img/arabe/car-9pm.webp",
                "pi-9pm", 99, null, 4.7, true, false, 2));

        repo.save(p("Khamrah", "Lattafa", "nicho-arabe", "unisex",
                "Oriental Especiado · Unisex", "Canela · Dátiles · Praliné · Vainilla",
                "/img/arabe/khamrah.webp", "/img/arabe/car-khamrah.webp",
                "pi-khamrah", 99, null, 4.8, true, false, 3));

        repo.save(p("Yara", "Lattafa", "nicho-arabe", "mujer",
                "Floral Gourmand · Mujer", "Orquídea · Mandarina · Vainilla · Sándalo",
                "/img/arabe/yara.webp", "/img/arabe/car-yara.webp",
                "pi-yara", 89, null, 4.6, true, false, 4));
    }

    private Producto p(String nombre, String marca, String categoria, String genero,
                       String familia, String notas,
                       String imgPrincipal, String imgCar,
                       String claseCss, int precio, Integer precio5ml,
                       double calificacion, boolean bestSeller, boolean soloItem, int orden) {
        Producto pr = new Producto();
        pr.setNombre(nombre);
        pr.setMarca(marca);
        pr.setCategoria(categoria);
        pr.setGenero(genero);
        pr.setFamilia(familia);
        pr.setNotas(notas);
        pr.setImagenPrincipal(imgPrincipal);
        pr.setImagenCaracteristicas(imgCar);
        pr.setClaseCss(claseCss);
        pr.setPrecio(precio);
        pr.setPrecio5ml(precio5ml);
        pr.setCalificacion(calificacion);
        pr.setBestSeller(bestSeller);
        pr.setSoloItem(soloItem);
        pr.setActivo(true);
        pr.setOrden(orden);
        return pr;
    }
}
