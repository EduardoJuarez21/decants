package mx.decants;

import mx.decants.entity.Kit;
import mx.decants.entity.Producto;
import mx.decants.repository.KitRepository;
import mx.decants.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductoRepository repo;
    private final KitRepository kitRepo;

    public DataInitializer(ProductoRepository repo, KitRepository kitRepo) {
        this.repo = repo;
        this.kitRepo = kitRepo;
    }

    @Override
    public void run(String... args) {
        seedKits();
        if (repo.existsByCategoria("alta-perfumeria")) return;

        // ── Alta Perfumería ──────────────────────────────────────────────────

        repo.save(p("Acqua di Giò", "Giorgio Armani", "alta-perfumeria", "hombre",
                "Acuático Aromático · Hombre", "Bergamota · Neroli · Mar · Pachulí",
                "/img/alta-perfumeria/hombre/aqua-di-gio.webp",
                "/img/alta-perfumeria/hombre/car-aqua-di-gio.webp",
                "pi-acqua", 310, 160, 4.9, false, false, 1));

        repo.save(p("Light Blue", "Dolce & Gabbana", "alta-perfumeria", "mujer",
                "Floral Frutal · Mujer", "Manzana · Cedro · Bambú · Ámbar",
                "/img/alta-perfumeria/mujer/light-blue.webp",
                "/img/alta-perfumeria/mujer/car-light-blue.webp",
                "pi-lightblue", 160, null, 4.8, false, false, 2));

        repo.save(p("Ombré Leather", "Tom Ford", "alta-perfumeria", "hombre",
                "Cuero Amaderado · Hombre", "Cuero · Cardamomo · Jazmín · Patchouli",
                "/img/alta-perfumeria/hombre/ombre-leather.webp",
                "/img/alta-perfumeria/hombre/car-ombre-leather.webp",
                "pi-tomford", 290, null, 4.9, false, false, 3));

        repo.save(p("Donna Born in Roma", "Valentino", "alta-perfumeria", "mujer",
                "Floral Gourmand · Mujer", "Jazmín · Vainilla · Vetiver · Iris",
                "/img/alta-perfumeria/mujer/valentino.webp",
                "/img/alta-perfumeria/mujer/car-valentino.webp",
                "pi-valentino", 220, null, 4.7, false, false, 4));

        repo.save(p("Cloud", "Ariana Grande", "alta-perfumeria", "mujer",
                "Floral Frutal · Mujer", "Lavanda · Pera · Coco · Praliné",
                "/img/alta-perfumeria/mujer/ariana-grande-cloud.webp",
                "/img/alta-perfumeria/mujer/car-ariana-grande-cloud.webp",
                "pi-cloud", 219, null, 4.5, true, true, 5));

        repo.save(p("Bleu de Chanel", "Chanel", "alta-perfumeria", "hombre",
                "Aromático Amaderado · Hombre", "Limón · Jengibre · Sándalo · Cedro",
                "/img/alta-perfumeria/hombre/blue-de-chanel.webp",
                "/img/alta-perfumeria/hombre/car-blue-de-chanel.webp",
                "pi-bleu", 250, null, 4.9, false, false, 6));

        repo.save(p("Sauvage", "Dior", "alta-perfumeria", "hombre",
                "Aromático Especiado · Hombre", "Bergamota · Pimienta · Ambroxan · Vetiver",
                "/img/alta-perfumeria/hombre/sauvage.webp",
                "/img/alta-perfumeria/hombre/car-sauvage.webp",
                "pi-sauvage", 240, null, 4.9, false, false, 7));

        repo.save(p("Le Male", "Jean Paul Gaultier", "alta-perfumeria", "hombre",
                "Oriental Fougère · Hombre", "Lavanda · Menta · Vainilla · Ámbar",
                "/img/alta-perfumeria/hombre/paul-gaultier.webp",
                "/img/alta-perfumeria/hombre/car-paul-gaultier.webp",
                "pi-lemale", 190, null, 4.8, false, false, 8));

        repo.save(p("1 Million", "Paco Rabanne", "alta-perfumeria", "hombre",
                "Oriental Especiado · Hombre", "Mandarina · Canela · Cuero · Pachulí",
                "/img/alta-perfumeria/hombre/one-million.webp",
                "/img/alta-perfumeria/hombre/car-one-million.webp",
                "pi-million", 190, null, 4.8, false, false, 9));

        repo.save(p("Black Opium", "Yves Saint Laurent", "alta-perfumeria", "mujer",
                "Oriental Gourmand · Mujer", "Café · Vainilla · Flor Blanca · Cedro",
                "/img/alta-perfumeria/mujer/black-opium.webp",
                "/img/alta-perfumeria/mujer/car-black-opium.webp",
                "pi-blackopium", 200, null, 4.8, false, false, 10));

        repo.save(p("Coco Mademoiselle", "Chanel", "alta-perfumeria", "mujer",
                "Oriental Floral · Mujer", "Naranja · Rosa · Jazmín · Pachulí",
                "/img/alta-perfumeria/mujer/coco-chanel.webp",
                "/img/alta-perfumeria/mujer/car-coco-chanel.webp",
                "pi-coco", 280, null, 4.9, false, false, 11));

        repo.save(p("Daisy", "Marc Jacobs", "alta-perfumeria", "mujer",
                "Floral Frutal · Mujer", "Fresa · Violeta · Jazmín · Sándalo",
                "/img/alta-perfumeria/mujer/daysi.webp",
                "/img/alta-perfumeria/mujer/car-daysi.webp",
                "pi-daisy", 180, null, 4.7, false, false, 12));

        repo.save(p("Good Girl", "Carolina Herrera", "alta-perfumeria", "mujer",
                "Oriental Floral · Mujer", "Jazmín · Cacao · Tonka · Café",
                "/img/alta-perfumeria/mujer/good-girl.webp",
                "/img/alta-perfumeria/mujer/car-good-girl.webp",
                "pi-goodgirl", 220, null, 4.8, false, false, 13));

        repo.save(p("J'adore", "Dior", "alta-perfumeria", "mujer",
                "Floral Afrutado · Mujer", "Ylang Ylang · Rosa · Jazmín · Vainilla",
                "/img/alta-perfumeria/mujer/jadore.webp",
                "/img/alta-perfumeria/mujer/car-jadore.webp",
                "pi-jadore", 240, null, 4.8, false, false, 14));

        repo.save(p("Libre", "Yves Saint Laurent", "alta-perfumeria", "mujer",
                "Floral Amaderado · Mujer", "Lavanda · Naranja · Jazmín · Musgo",
                "/img/alta-perfumeria/mujer/libre.webp",
                "/img/alta-perfumeria/mujer/car-libre.webp",
                "pi-libre", 220, null, 4.8, false, false, 15));

        repo.save(p("Sì", "Giorgio Armani", "alta-perfumeria", "mujer",
                "Chypre Floral · Mujer", "Grosella negra · Rosa · Pachulí · Vainilla",
                "/img/alta-perfumeria/mujer/si-giorgio-armani.webp",
                "/img/alta-perfumeria/mujer/car-si-giorgio-armani.webp",
                "pi-si", 200, null, 4.7, false, false, 16));

        repo.save(p("Flowerbomb", "Viktor & Rolf", "alta-perfumeria", "mujer",
                "Floral Oriental · Mujer", "Bergamota · Jazmín · Rosa · Pachulí",
                "/img/alta-perfumeria/mujer/viktor.webp",
                "/img/alta-perfumeria/mujer/car-viktor.webp",
                "pi-flowerbomb", 230, null, 4.8, false, false, 17));

        repo.save(p("Acqua di Giò Profondo", "Giorgio Armani", "alta-perfumeria", "hombre",
                "Aromático Acuático · Hombre", "Mar · Bergamota · Mandarina · Romero · Lavanda · Pachulí · Ámbar",
                "/img/alta-perfumeria/hombre/acqua-di-gio-profondo.webp",
                "/img/alta-perfumeria/hombre/car-acqua-di-gio-profondo.webp",
                "pi-profondo", 420, 230, 4.8, false, false, 18));

        repo.save(p("Acqua di Giò Parfum", "Giorgio Armani", "alta-perfumeria", "hombre",
                "Acuático Amaderado · Hombre", "Marina · Bergamota · Romero · Salvia · Geranio · Olíbano · Pachulí",
                "/img/alta-perfumeria/hombre/acqua-di-gio-parfum.webp",
                "/img/alta-perfumeria/hombre/car-acqua-di-gio-parfum.webp",
                "pi-parfum", 420, 230, 4.8, false, false, 19));

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

        repo.save(p("Hawas", "Rasasi", "nicho-arabe", "hombre",
                "Acuático Amaderado · Hombre", "Menta · Manzana · Cardamomo · Cedro · Ámbar",
                "/img/arabe/hawas.webp", "/img/arabe/car-hawas.webp",
                "pi-hawas", 999, null, 4.8, true, false, 5));

        repo.save(p("L'Aventure", "Al Haramain", "nicho-arabe", "hombre",
                "Aromático Fresco · Hombre", "Bergamota · Lavanda · Madera · Vetiver · Almizcle",
                "/img/arabe/laventure.webp", "/img/arabe/car-laventure.webp",
                "pi-laventure", 999, null, 4.7, false, false, 6));

        repo.save(p("Supremacy Silver", "Afnan", "nicho-arabe", "hombre",
                "Acuático Floral · Hombre", "Bergamota · Jazmín · Cedro · Sándalo · Almizcle",
                "/img/arabe/supremacy-silver.webp", "/img/arabe/car-supremacy-silver.webp",
                "pi-supremacy-silver", 999, null, 4.6, false, false, 7));

        repo.save(p("Asad", "Lattafa", "nicho-arabe", "hombre",
                "Amaderado Oriental · Hombre", "Cuero · Oud · Especias · Vainilla · Ámbar",
                "/img/arabe/asad.webp", "/img/arabe/car-asad.webp",
                "pi-asad", 999, null, 4.7, false, false, 8));

        repo.save(p("Supremacy Pink", "Afnan", "nicho-arabe", "mujer",
                "Frutal Floral · Mujer", "Litchi · Fresia · Rosa · Almizcle · Sándalo",
                "/img/arabe/supremacy-pink.webp", "/img/arabe/car-supremacy-pink.webp",
                "pi-supremacy-pink", 999, null, 4.6, false, false, 9));

        repo.save(p("La Yuqawam Femme", "Rasasi", "nicho-arabe", "mujer",
                "Floral Oriental · Mujer", "Bergamota · Rosa · Jazmín · Pachulí · Ámbar",
                "/img/arabe/la-yuqawam-femme.webp", "/img/arabe/car-la-yuqawam-femme.webp",
                "pi-layuqawam", 999, null, 4.7, false, false, 10));

        repo.save(p("Lail Maleki", "Lattafa", "nicho-arabe", "mujer",
                "Oriental Floral · Mujer", "Rosa · Oud · Sándalo · Vainilla · Ámbar",
                "/img/arabe/lail-maleki.webp", "/img/arabe/car-lail-maleki.webp",
                "pi-lailmaleki", 999, null, 4.7, false, false, 11));

        repo.save(p("Yara Moi", "Lattafa", "nicho-arabe", "mujer",
                "Floral Gourmand · Mujer", "Cereza · Rosa · Vainilla · Caramelo · Almizcle",
                "/img/arabe/yara-moi.webp", "/img/arabe/car-yara-moi.webp",
                "pi-yaramoi", 999, null, 4.7, true, false, 12));

        repo.save(p("Nouf", "Swiss Arabian", "nicho-arabe", "mujer",
                "Floral Amaderado · Mujer", "Rosa · Jazmín · Cedro · Sándalo · Almizcle",
                "/img/arabe/nouf.webp", "/img/arabe/car-nouf.webp",
                "pi-nouf", 999, null, 4.6, false, false, 13));

        repo.save(p("Amber Oud", "Al Haramain", "nicho-arabe", "unisex",
                "Oriental Amaderado · Unisex", "Oud · Ámbar · Rosa · Sándalo · Almizcle",
                "/img/arabe/amber-oud.webp", "/img/arabe/car-amber-oud.webp",
                "pi-amberoud", 999, null, 4.9, true, false, 14));

        repo.save(p("Raghba", "Lattafa", "nicho-arabe", "unisex",
                "Oriental Dulce · Unisex", "Vainilla · Ámbar · Cuero · Madera · Almizcle",
                "/img/arabe/raghba.webp", "/img/arabe/car-raghba.webp",
                "pi-raghba", 999, null, 4.7, false, false, 15));
    }

    private void seedKits() {
        if (kitRepo.existsBySlug("discovery-esencial")) return;

        kitRepo.save(k("discovery-esencial",    "Discovery Esencial",         279, false,  1));
        kitRepo.save(k("coleccion-esencial",     "Colección Esencial",         449, false,  2));
        kitRepo.save(k("travel-set-esencial",    "Travel Set Esencial",        499, false,  3));
        kitRepo.save(k("kit-arabe",              "Kit Árabe",                  279, false,  4));
        kitRepo.save(k("kit-oficina",            "Kit Oficina",                499, true,   5));
        kitRepo.save(k("kit-dulce",              "Kit Dulce",                  429, true,   6));
        kitRepo.save(k("kit-elegante",           "Kit Elegante",               649, true,   7));
        kitRepo.save(k("kit-para-citas",         "Kit Para Citas",             499, true,   8));
        kitRepo.save(k("discovery-premium",      "Discovery Premium",          599, true,   9));
        kitRepo.save(k("regalo-personalizado",   "Regalo Personalizado",       449, true,  10));
        kitRepo.save(k("aura-box-mensual",       "Aura Box Mensual",          null, false, 11));
        kitRepo.save(k("caja-regalo-premium",    "Caja de Regalo Premium",     699, true,  12));
    }

    private Kit k(String slug, String nombre, Integer precio, boolean desde, int orden) {
        Kit kit = new Kit();
        kit.setSlug(slug);
        kit.setNombre(nombre);
        kit.setPrecio(precio);
        kit.setPrecioDesde(desde);
        kit.setActivo(true);
        kit.setOrden(orden);
        return kit;
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
