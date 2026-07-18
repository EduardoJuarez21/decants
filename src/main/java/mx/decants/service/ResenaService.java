package mx.decants.service;

import mx.decants.entity.Resena;
import mx.decants.repository.ResenaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResenaService {

    private final ResenaRepository resenaRepository;

    public ResenaService(ResenaRepository resenaRepository) {
        this.resenaRepository = resenaRepository;
    }

    public void guardar(String nombre, String texto, int calificacion) {
        Resena r = new Resena();
        r.setNombre(nombre.trim());
        r.setTexto(texto.trim());
        r.setCalificacion(Math.max(1, Math.min(5, calificacion)));
        resenaRepository.save(r);
    }

    public List<Resena> listarAprobadas() {
        return resenaRepository.findByAprobadaTrueOrderByFechaDesc();
    }

    public List<Resena> listarTodas() {
        return resenaRepository.findAllByOrderByFechaDesc();
    }

    public void aprobar(Long id) {
        resenaRepository.findById(id).ifPresent(r -> {
            r.setAprobada(true);
            resenaRepository.save(r);
        });
    }

    public void eliminar(Long id) {
        resenaRepository.deleteById(id);
    }

    public long contarPendientes() {
        return resenaRepository.countByAprobadaFalse();
    }
}
