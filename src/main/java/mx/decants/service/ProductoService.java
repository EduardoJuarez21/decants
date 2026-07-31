package mx.decants.service;

import mx.decants.entity.Producto;
import mx.decants.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductoService {

    // El buscador del sitio es del lado del cliente y solo filtra lo ya renderizado,
    // así que el tamaño de página debe cubrir todo el catálogo activo por categoría
    // para que buscar no dependa de en qué página esté el producto.
    private static final int PAGE_SIZE = 50;

    private final ProductoRepository productoRepository;
    private final ConfiguracionService configuracionService;

    public ProductoService(ProductoRepository productoRepository, ConfiguracionService configuracionService) {
        this.productoRepository = productoRepository;
        this.configuracionService = configuracionService;
    }

    @Transactional(readOnly = true)
    public List<Producto> activosPorCategoria(String categoria) {
        return productoRepository.findByCategoriaAndActivoTrueOrderByNombreAsc(categoria);
    }

    @Transactional(readOnly = true)
    public Page<Producto> activosPorCategoriaPaginados(String categoria, String genero, int page) {
        var pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("nombre"));
        if (genero == null || genero.isBlank() || "todos".equals(genero)) {
            return productoRepository.findByCategoriaAndActivoTrue(categoria, pageable);
        }
        if ("mujer".equals(genero)) {
            return productoRepository.findByCategoriaAndGeneroInAndActivoTrue(
                    categoria, List.of("mujer", "unisex"), pageable);
        }
        return productoRepository.findByCategoriaAndGeneroAndActivoTrue(categoria, genero, pageable);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAllByOrderByCategoriaAscOrdenAsc();
    }

    @Transactional(readOnly = true)
    public List<Producto> activosTodos() {
        return productoRepository.findByActivoTrueOrderByCategoriaAscOrdenAsc();
    }

    @Transactional(readOnly = true)
    public byte[] exportarCsvParaIA(List<Producto> productos, java.util.Map<Long, String> archivoPorProducto) {
        StringBuilder sb = new StringBuilder();
        sb.append("Archivo,Marca,Nombre,Categoria,Genero,Familia,Notas,Caracteristicas,Inspiracion,")
          .append("Precio3ml,Precio5ml,Precio10ml,PromoActiva,DescuentoPorcentaje\n");
        for (Producto p : productos) {
            sb.append(csv(archivoPorProducto.get(p.getId())))
              .append(",").append(csv(p.getMarca()))
              .append(",").append(csv(p.getNombre()))
              .append(",").append(csv(p.getCategoria()))
              .append(",").append(csv(p.getGenero()))
              .append(",").append(csv(p.getFamilia()))
              .append(",").append(csv(p.getNotas()))
              .append(",").append(csv(p.getCaracteristicas()))
              .append(",").append(csv(p.getInspiracion()))
              .append(",").append(csv(p.getPrecio3ml()))
              .append(",").append(csv(p.getPrecio5ml()))
              .append(",").append(csv(p.getPrecio()))
              .append(",").append(csv(p.isPromoActivo() ? "Si" : "No"))
              .append(",").append(csv(p.getDescuentoPorcentaje()))
              .append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String csv(Object val) {
        if (val == null) return "";
        String s = val.toString().replace("\"", "\"\"");
        return s.contains(",") || s.contains("\"") || s.contains("\n") ? "\"" + s + "\"" : s;
    }

    public void toggleActivo(Long id) {
        productoRepository.findById(id).ifPresent(p -> {
            p.setActivo(!p.isActivo());
            productoRepository.save(p);
        });
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    public void actualizar(Long id, Integer precio, Integer precio5ml, Integer precio3ml, String nombre, String marca, boolean bestSeller, String caracteristicas, String inspiracion,
                           boolean promoActivo, Integer descuentoPorcentaje, String proveedor, Double costoPorMl, Double markup, String concentracion,
                           Integer precioBotella, Integer mlBotella,
                           Double comisionFamiliar, Double comisionFamiliar5ml, Double comisionFamiliar3ml) {
        productoRepository.findById(id).ifPresent(p -> {
            if (nombre != null && !nombre.isBlank()) p.setNombre(nombre.trim());
            if (marca  != null && !marca.isBlank())  p.setMarca(marca.trim());
            if (precio != null && precio > 0)         p.setPrecio(precio);
            p.setPrecio5ml(precio5ml != null && precio5ml > 0 ? precio5ml : null);
            p.setPrecio3ml(precio3ml != null && precio3ml > 0 ? precio3ml : null);
            p.setBestSeller(bestSeller);
            p.setCaracteristicas(caracteristicas != null && !caracteristicas.isBlank() ? caracteristicas.trim() : null);
            p.setInspiracion(inspiracion != null && !inspiracion.isBlank() ? inspiracion.trim() : null);
            p.setConcentracion(concentracion != null && !concentracion.isBlank() ? concentracion.trim() : null);
            p.setDescuentoPorcentaje(descuentoPorcentaje != null && descuentoPorcentaje > 0 && descuentoPorcentaje < 100 ? descuentoPorcentaje : null);
            p.setPromoActivo(promoActivo && p.getDescuentoPorcentaje() != null);
            p.setProveedor(proveedor != null && !proveedor.isBlank() ? proveedor.trim() : null);
            p.setCostoPorMl(costoPorMl != null && costoPorMl > 0 ? costoPorMl : null);
            p.setMarkup(markup != null && markup > 0 ? markup : null);
            p.setPrecioBotella(precioBotella != null && precioBotella > 0 ? precioBotella : null);
            p.setMlBotella(mlBotella != null && mlBotella > 0 ? mlBotella : null);
            p.setComisionFamiliar(comisionFamiliar != null && comisionFamiliar > 0 ? comisionFamiliar : null);
            p.setComisionFamiliar5ml(comisionFamiliar5ml != null && comisionFamiliar5ml > 0 ? comisionFamiliar5ml : null);
            p.setComisionFamiliar3ml(comisionFamiliar3ml != null && comisionFamiliar3ml > 0 ? comisionFamiliar3ml : null);
            productoRepository.save(p);
        });
    }

    public void actualizarStockBotella(Long id, Integer stockBotella) {
        productoRepository.findById(id).ifPresent(p -> {
            p.setStockBotella(stockBotella != null && stockBotella >= 0 ? stockBotella : null);
            productoRepository.save(p);
        });
    }

    public void togglePromo(Long id) {
        productoRepository.findById(id).ifPresent(p -> {
            p.setPromoActivo(!p.isPromoActivo() && p.getDescuentoPorcentaje() != null);
            productoRepository.save(p);
        });
    }

    public void actualizarStock(Long id, Integer stock) {
        productoRepository.findById(id).ifPresent(p -> {
            p.setStock(stock != null && stock >= 0 ? stock : null);
            productoRepository.save(p);
        });
    }

    public Producto crear(String nombre, String marca, String categoria, String genero,
                          String familia, String notas, String caracteristicas, Integer precio, Integer precio5ml,
                          boolean bestSeller, String imagenPrincipal, String imagenCaracteristicas, int orden,
                          String proveedor, Double costoPorMl, Double markup, String concentracion) {
        Producto p = new Producto();
        p.setNombre(nombre.trim());
        p.setMarca(marca.trim());
        p.setCategoria(categoria);
        p.setGenero(genero);
        p.setFamilia(familia != null && !familia.isBlank() ? familia.trim() : "");
        p.setNotas(notas != null && !notas.isBlank() ? notas.trim() : "");
        p.setCaracteristicas(caracteristicas != null && !caracteristicas.isBlank() ? caracteristicas.trim() : null);
        p.setConcentracion(concentracion != null && !concentracion.isBlank() ? concentracion.trim() : null);
        p.setPrecio(precio);
        p.setPrecio5ml(precio5ml != null && precio5ml > 0 ? precio5ml : null);
        p.setPrecio3ml(null);
        p.setBestSeller(bestSeller);
        p.setImagenPrincipal(imagenPrincipal);
        p.setImagenCaracteristicas(imagenCaracteristicas);
        p.setClaseCss("pi-" + slugificar(nombre.trim()));
        p.setCalificacion(4.8);
        p.setSoloItem(false);
        p.setActivo(true);
        p.setOrden(orden);
        p.setProveedor(proveedor != null && !proveedor.isBlank() ? proveedor.trim() : null);
        p.setCostoPorMl(costoPorMl != null && costoPorMl > 0 ? costoPorMl : null);
        p.setMarkup(markup != null && markup > 0 ? markup : configuracionService.getMarkupDefault());
        return productoRepository.save(p);
    }

    private static String slugificar(String texto) {
        return texto.toLowerCase()
            .replaceAll("[áàäâã]", "a").replaceAll("[éèëê]", "e")
            .replaceAll("[íìïî]", "i").replaceAll("[óòöôõ]", "o")
            .replaceAll("[úùüû]", "u").replaceAll("[ñ]", "n")
            .replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
