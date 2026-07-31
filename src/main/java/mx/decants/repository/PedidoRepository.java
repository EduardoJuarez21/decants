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
}
