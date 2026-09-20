package mx.decants.repository;

import mx.decants.entity.VendedorCobro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendedorCobroRepository extends JpaRepository<VendedorCobro, Long> {

    Optional<VendedorCobro> findByVendedorAndMes(String vendedor, String mes);

    void deleteByVendedorAndMes(String vendedor, String mes);

    @Query("select coalesce(sum(c.montoCobrado), 0) from VendedorCobro c where c.vendedor = :vendedor")
    int sumMontoCobradoByVendedor(@Param("vendedor") String vendedor);
}
