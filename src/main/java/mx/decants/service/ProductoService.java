package mx.decants.service;

import mx.decants.entity.Producto;
import mx.decants.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> activosPorCategoria(String categoria) {
        return productoRepository.findByCategoriaAndActivoTrueOrderByNombreAsc(categoria);
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
}
