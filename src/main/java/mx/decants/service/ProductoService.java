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

    private static final int PAGE_SIZE = 10;

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
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

    public void actualizar(Long id, Integer precio, Integer precio5ml, Integer precio3ml, String nombre, String marca, boolean bestSeller, String caracteristicas, String inspiracion) {
        productoRepository.findById(id).ifPresent(p -> {
            if (nombre != null && !nombre.isBlank()) p.setNombre(nombre.trim());
            if (marca  != null && !marca.isBlank())  p.setMarca(marca.trim());
            if (precio != null && precio > 0)         p.setPrecio(precio);
            p.setPrecio5ml(precio5ml != null && precio5ml > 0 ? precio5ml : null);
            p.setPrecio3ml(precio3ml != null && precio3ml > 0 ? precio3ml : null);
            p.setBestSeller(bestSeller);
            p.setCaracteristicas(caracteristicas != null && !caracteristicas.isBlank() ? caracteristicas.trim() : null);
            p.setInspiracion(inspiracion != null && !inspiracion.isBlank() ? inspiracion.trim() : null);
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
                          boolean bestSeller, String imagenPrincipal, String imagenCaracteristicas, int orden) {
        Producto p = new Producto();
        p.setNombre(nombre.trim());
        p.setMarca(marca.trim());
        p.setCategoria(categoria);
        p.setGenero(genero);
        p.setFamilia(familia != null && !familia.isBlank() ? familia.trim() : "");
        p.setNotas(notas != null && !notas.isBlank() ? notas.trim() : "");
        p.setCaracteristicas(caracteristicas != null && !caracteristicas.isBlank() ? caracteristicas.trim() : null);
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
