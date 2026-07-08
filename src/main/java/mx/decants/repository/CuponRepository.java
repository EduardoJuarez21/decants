package mx.decants.repository;

import mx.decants.entity.Cupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuponRepository extends JpaRepository<Cupon, Long> {
    Optional<Cupon> findByCodigoIgnoreCaseAndActivoTrue(String codigo);
    List<Cupon> findAllByOrderByIdDesc();
}
