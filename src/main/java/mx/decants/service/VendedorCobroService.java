package mx.decants.service;

import mx.decants.entity.VendedorCobro;
import mx.decants.repository.VendedorCobroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

@Service
@Transactional
public class VendedorCobroService {

    private final VendedorCobroRepository repo;

    public VendedorCobroService(VendedorCobroRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public Optional<VendedorCobro> buscar(String vendedor, YearMonth mes) {
        return repo.findByVendedorAndMes(vendedor, mes.toString());
    }

    @Transactional(readOnly = true)
    public int totalCobradoAcumulado(String vendedor) {
        return repo.sumMontoCobradoByVendedor(vendedor);
    }

    // Acumula sobre lo ya cobrado ese mes en vez de reemplazarlo, igual que
    // ComisionPagoService.marcarPagado, para poder registrar abonos parciales.
    public void marcarCobrado(String vendedor, YearMonth mes, Integer monto, String notas) {
        VendedorCobro cobro = repo.findByVendedorAndMes(vendedor, mes.toString()).orElseGet(VendedorCobro::new);
        cobro.setVendedor(vendedor);
        cobro.setMes(mes.toString());
        int montoPrevio = cobro.getMontoCobrado() != null ? cobro.getMontoCobrado() : 0;
        cobro.setMontoCobrado(montoPrevio + monto);
        String notaNueva = notas != null && !notas.isBlank() ? notas.trim() : null;
        if (cobro.getNotas() != null && notaNueva != null) {
            cobro.setNotas(cobro.getNotas() + " · " + notaNueva);
        } else if (notaNueva != null) {
            cobro.setNotas(notaNueva);
        }
        cobro.setFechaCobro(LocalDateTime.now());
        repo.save(cobro);
    }

    public void desmarcar(String vendedor, YearMonth mes) {
        repo.deleteByVendedorAndMes(vendedor, mes.toString());
    }
}
