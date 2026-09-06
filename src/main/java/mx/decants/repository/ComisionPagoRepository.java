package mx.decants.repository;

import mx.decants.entity.ComisionPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComisionPagoRepository extends JpaRepository<ComisionPago, Long> {

    Optional<ComisionPago> findByVendedorAndMes(String vendedor, String mes);

    void deleteByVendedorAndMes(String vendedor, String mes);

    @Query("select coalesce(sum(c.montoPagado), 0) from ComisionPago c where c.vendedor = :vendedor")
    int sumMontoPagadoByVendedor(@Param("vendedor") String vendedor);
}
