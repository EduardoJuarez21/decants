package mx.decants.repository;

import mx.decants.entity.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendedorRepository extends JpaRepository<Vendedor, Long> {
    Optional<Vendedor> findByUsuarioIgnoreCase(String usuario);
    List<Vendedor> findAllByOrderByNombreAsc();
    List<Vendedor> findAllByActivoTrueOrderByNombreAsc();
}
