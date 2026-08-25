package mx.decants.service;

import mx.decants.entity.ComisionPago;
import mx.decants.repository.ComisionPagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

@Service
@Transactional
public class ComisionPagoService {

    private final ComisionPagoRepository repo;

    public ComisionPagoService(ComisionPagoRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public Optional<ComisionPago> buscar(String vendedor, YearMonth mes) {
        return repo.findByVendedorAndMes(vendedor, mes.toString());
    }

    // Acumula sobre lo ya pagado ese mes en vez de reemplazarlo, para poder
    // registrar un abono adicional cuando entran ventas nuevas despues de
    // haber marcado el mes como pagado (sin perder el pago anterior).
    public void marcarPagado(String vendedor, YearMonth mes, Integer monto, String notas) {
        ComisionPago pago = repo.findByVendedorAndMes(vendedor, mes.toString()).orElseGet(ComisionPago::new);
        pago.setVendedor(vendedor);
        pago.setMes(mes.toString());
        int montoPrevio = pago.getMontoPagado() != null ? pago.getMontoPagado() : 0;
        pago.setMontoPagado(montoPrevio + monto);
        String notaNueva = notas != null && !notas.isBlank() ? notas.trim() : null;
        if (pago.getNotas() != null && notaNueva != null) {
            pago.setNotas(pago.getNotas() + " · " + notaNueva);
        } else if (notaNueva != null) {
            pago.setNotas(notaNueva);
        }
        pago.setFechaPago(LocalDateTime.now());
        repo.save(pago);
    }

    public void desmarcar(String vendedor, YearMonth mes) {
        repo.deleteByVendedorAndMes(vendedor, mes.toString());
    }
}
