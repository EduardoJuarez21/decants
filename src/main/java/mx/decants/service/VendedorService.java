package mx.decants.service;

import mx.decants.entity.Vendedor;
import mx.decants.repository.VendedorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VendedorService {

    private final VendedorRepository vendedorRepository;
    private final PasswordEncoder passwordEncoder;

    public VendedorService(VendedorRepository vendedorRepository, PasswordEncoder passwordEncoder) {
        this.vendedorRepository = vendedorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Vendedor> listarTodos() {
        return vendedorRepository.findAllByOrderByNombreAsc();
    }

    public List<Vendedor> listarActivos() {
        return vendedorRepository.findAllByActivoTrueOrderByNombreAsc();
    }

    public Optional<Vendedor> buscarPorUsuario(String usuario) {
        return vendedorRepository.findByUsuarioIgnoreCase(usuario);
    }

    public Vendedor crear(String usuario, String passwordPlano, String nombre, int metaMonto, String metaPremio) {
        Vendedor v = new Vendedor();
        v.setUsuario(usuario.trim().toLowerCase());
        v.setPasswordHash(passwordEncoder.encode(passwordPlano));
        v.setNombre(nombre.trim());
        v.setMetaMonto(metaMonto);
        v.setMetaPremio(metaPremio);
        return vendedorRepository.save(v);
    }

    public void actualizar(Long id, String nombre, int metaMonto, String metaPremio) {
        vendedorRepository.findById(id).ifPresent(v -> {
            v.setNombre(nombre.trim());
            v.setMetaMonto(metaMonto);
            v.setMetaPremio(metaPremio);
            vendedorRepository.save(v);
        });
    }

    public void cambiarPassword(Long id, String passwordPlano) {
        vendedorRepository.findById(id).ifPresent(v -> {
            v.setPasswordHash(passwordEncoder.encode(passwordPlano));
            vendedorRepository.save(v);
        });
    }

    public void toggleActivo(Long id) {
        vendedorRepository.findById(id).ifPresent(v -> {
            v.setActivo(!v.isActivo());
            vendedorRepository.save(v);
        });
    }
}
