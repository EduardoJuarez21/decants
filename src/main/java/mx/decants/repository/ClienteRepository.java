package mx.decants.repository;

import mx.decants.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByTelefono(String telefono);

    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.pedidos ORDER BY c.fechaActualizacion DESC")
    List<Cliente> findAllWithPedidos();
}
