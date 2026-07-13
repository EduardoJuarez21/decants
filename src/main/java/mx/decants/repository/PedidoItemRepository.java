package mx.decants.repository;

import mx.decants.entity.PedidoItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {

    @Query("SELECT i.nombre, SUM(i.cantidad) FROM PedidoItem i " +
           "WHERE i.variante <> 'paquete' " +
           "GROUP BY i.nombre ORDER BY SUM(i.cantidad) DESC")
    List<Object[]> findTopProductos(Pageable pageable);
}