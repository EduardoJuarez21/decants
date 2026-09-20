package mx.decants.repository;

import mx.decants.entity.EstadoPedido;
import mx.decants.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    // fechaEntrega es la señal real de "aqui empieza la deuda de la vendedora" --
    // normalmente se llena al llegar a ENTREGADO, pero tambien se puede marcar a
    // mano (ver PedidoService.marcarDeudaManual) sin depender del estado de envio.
    List<Pedido> findByVendedorAndFechaEntregaIsNotNullAndEstadoPedidoNot(
        String vendedor, EstadoPedido estadoExcluido);

    List<Pedido> findByVendedorAndFechaEntregaBetweenAndEstadoPedidoNot(
        String vendedor, LocalDateTime desde, LocalDateTime hasta, EstadoPedido estadoExcluido);
}
