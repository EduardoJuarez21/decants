package mx.decants.repository;

import mx.decants.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoriaAndActivoTrueOrderByOrdenAsc(String categoria);

    List<Producto> findAllByOrderByCategoriaAscOrdenAsc();

    boolean existsByCategoria(String categoria);
}
