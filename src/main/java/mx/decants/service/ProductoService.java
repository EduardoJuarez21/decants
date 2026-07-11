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
}
