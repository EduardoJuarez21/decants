package mx.decants.repository;

import mx.decants.entity.EstadoPedido;
import mx.decants.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findAllByOrderByFechaCreacionDesc();

    Optional<Pedido> findByStripeSessionId(String stripeSessionId);

    Optional<Pedido> findByCodigoPublicoAndTelefono(String codigoPublico, String telefono);

    List<Pedido> findByVendedorAndFechaCreacionAfterAndEstadoPedidoNot(
        String vendedor, LocalDateTime desde, EstadoPedido estadoExcluido);

    List<Pedido> findByVendedorAndFechaCreacionBetweenAndEstadoPedidoNot(
        String vendedor, LocalDateTime desde, LocalDateTime hasta, EstadoPedido estadoExcluido);

    List<Pedido> findByVendedorAndEstadoPedidoNot(String vendedor, EstadoPedido estadoExcluido);

    // Deuda actual de la vendedora: pedidos que aun no llegan a Entregado (que es
    // cuando, segun el flujo real, su cliente le paga y por lo tanto ella ya nos
    // debe ese dinero) y que no esten Cancelados.
    List<Pedido> findByVendedorAndEstadoPedidoNotIn(String vendedor, Collection<EstadoPedido> estadosExcluidos);
}
