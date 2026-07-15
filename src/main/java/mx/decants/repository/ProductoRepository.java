package mx.decants.repository;

import mx.decants.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoriaAndActivoTrueOrderByNombreAsc(String categoria);

    List<Producto> findAllByOrderByCategoriaAscOrdenAsc();

    boolean existsByCategoria(String categoria);

    Page<Producto> findByCategoriaAndActivoTrue(String categoria, Pageable pageable);

    Page<Producto> findByCategoriaAndGeneroAndActivoTrue(String categoria, String genero, Pageable pageable);

    Page<Producto> findByCategoriaAndGeneroInAndActivoTrue(String categoria, List<String> generos, Pageable pageable);
}
