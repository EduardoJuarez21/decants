package mx.decants.repository;

import mx.decants.entity.Kit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KitRepository extends JpaRepository<Kit, Long> {
    boolean existsBySlug(String slug);
    List<Kit> findAllByOrderByOrdenAsc();
}
