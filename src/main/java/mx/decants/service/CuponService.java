package mx.decants.service;

import mx.decants.entity.Cupon;
import mx.decants.repository.CuponRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CuponService {

    private final CuponRepository cuponRepository;

    public CuponService(CuponRepository cuponRepository) {
        this.cuponRepository = cuponRepository;
    }

    public Optional<Cupon> validar(String codigo) {
        return cuponRepository.findByCodigoIgnoreCaseAndActivoTrue(codigo);
    }

    public List<Cupon> listarTodos() {
        return cuponRepository.findAllByOrderByIdDesc();
    }

    public Cupon guardar(Cupon cupon) {
        cupon.setCodigo(cupon.getCodigo().toUpperCase().trim());
        return cuponRepository.save(cupon);
    }

    public void toggleActivo(Long id) {
        cuponRepository.findById(id).ifPresent(c -> {
            c.setActivo(!c.isActivo());
            cuponRepository.save(c);
        });
    }

    public void eliminar(Long id) {
        cuponRepository.deleteById(id);
    }
}
