package mx.decants.repository;

import mx.decants.entity.EstadoPedido;
import mx.decants.entity.PedidoItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {

    @Query("SELECT i.nombre, SUM(i.cantidad) FROM PedidoItem i " +
           "WHERE i.variante <> 'paquete' " +
           "GROUP BY i.nombre ORDER BY SUM(i.cantidad) DESC")
    List<Object[]> findTopProductos(Pageable pageable);

    @Query("SELECT i.producto.id, SUM(i.cantidad) FROM PedidoItem i " +
           "WHERE i.producto IS NOT NULL AND i.variante <> 'paquete' " +
           "AND i.pedido.estadoPedido IN :estados " +
           "GROUP BY i.producto.id")
    List<Object[]> sumarVendidosPorProducto(@Param("estados") Collection<EstadoPedido> estados);
}