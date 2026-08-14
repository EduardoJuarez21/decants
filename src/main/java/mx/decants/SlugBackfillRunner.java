package mx.decants;

import mx.decants.entity.Producto;
import mx.decants.repository.ProductoRepository;
import mx.decants.service.ProductoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Red de seguridad: cualquier producto sin slug (los sembrados por
    DataInitializer antes de este cambio, o creados por una via que se
    salte ProductoService.crear) recibe uno al arrancar. No hace nada
    una vez que todos los productos ya tienen slug. */
@Component
public class SlugBackfillRunner implements CommandLineRunner {

    private final ProductoRepository productoRepository;
    private final ProductoService productoService;

    public SlugBackfillRunner(ProductoRepository productoRepository, ProductoService productoService) {
        this.productoRepository = productoRepository;
        this.productoService = productoService;
    }

    @Override
    public void run(String... args) {
        List<Producto> sinSlug = productoRepository.findBySlugIsNull();
        for (Producto p : sinSlug) {
            p.setSlug(productoService.generarSlugUnico(p.getMarca(), p.getNombre()));
            productoRepository.save(p);
        }
    }
}
