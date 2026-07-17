package mx.decants.repository;

import mx.decants.entity.Visita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitaRepository extends JpaRepository<Visita, Long> {

    @Query("SELECT v.fuente, COUNT(v) FROM Visita v WHERE v.fecha >= :desde GROUP BY v.fuente ORDER BY COUNT(v) DESC")
    List<Object[]> contarPorFuenteDesde(@Param("desde") LocalDateTime desde);

    long countByFechaAfter(LocalDateTime desde);
}
