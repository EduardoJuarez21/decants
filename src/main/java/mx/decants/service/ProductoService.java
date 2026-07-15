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

    private static final int PAGE_SIZE = 12;

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
        var pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("orden", "nombre"));
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

    public void actualizar(Long id, Integer precio, Integer precio5ml, String nombre, String marca, boolean bestSeller) {
        productoRepository.findById(id).ifPresent(p -> {
            if (nombre != null && !nombre.isBlank()) p.setNombre(nombre.trim());
            if (marca  != null && !marca.isBlank())  p.setMarca(marca.trim());
            if (precio != null && precio > 0)         p.setPrecio(precio);
            p.setPrecio5ml(precio5ml != null && precio5ml > 0 ? precio5ml : null);
            p.setBestSeller(bestSeller);
            productoRepository.save(p);
        });
    }

    public void actualizarStock(Long id, Integer stock) {
        productoRepository.findById(id).ifPresent(p -> {
            p.setStock(stock != null && stock >= 0 ? stock : null);
            productoRepository.save(p);
        });
    }
}
