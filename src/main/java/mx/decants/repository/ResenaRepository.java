package mx.decants.repository;

import mx.decants.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByAprobadaTrueOrderByFechaDesc();
    List<Resena> findAllByOrderByFechaDesc();
    long countByAprobadaFalse();
}
