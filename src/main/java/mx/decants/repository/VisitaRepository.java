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

    @Query("SELECT v.fuente, COUNT(v) FROM Visita v WHERE v.fecha >= :desde AND (v.tipo IS NULL OR v.tipo = 'pagina') GROUP BY v.fuente ORDER BY COUNT(v) DESC")
    List<Object[]> contarPorFuenteDesde(@Param("desde") LocalDateTime desde);

    @Query("SELECT COUNT(v) FROM Visita v WHERE v.fecha >= :desde AND (v.tipo IS NULL OR v.tipo = 'pagina')")
    long countByFechaAfter(@Param("desde") LocalDateTime desde);

    @Query(value = "SELECT CAST(fecha AS date) AS dia, COUNT(*) AS total FROM visitas WHERE fecha >= :desde AND (tipo IS NULL OR tipo = 'pagina') GROUP BY dia ORDER BY dia", nativeQuery = true)
    List<Object[]> visitasPorDia(@Param("desde") LocalDateTime desde);

    @Query("SELECT COUNT(v) FROM Visita v WHERE v.fecha >= :desde AND v.tipo = 'descarga_catalogo'")
    long countDescargasCatalogoByFechaAfter(@Param("desde") LocalDateTime desde);

    @Query("SELECT v.fuente, COUNT(v) FROM Visita v WHERE v.fecha >= :desde AND v.tipo = 'descarga_catalogo' GROUP BY v.fuente ORDER BY COUNT(v) DESC")
    List<Object[]> contarDescargasCatalogoPorFuenteDesde(@Param("desde") LocalDateTime desde);
}
