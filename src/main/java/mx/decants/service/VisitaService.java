package mx.decants.service;

import mx.decants.entity.Visita;
import mx.decants.repository.VisitaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VisitaService {

    private static final List<String> FUENTES_CONOCIDAS =
        List.of("qr", "instagram", "whatsapp", "facebook", "tiktok");

    private final VisitaRepository visitaRepository;

    public VisitaService(VisitaRepository visitaRepository) {
        this.visitaRepository = visitaRepository;
    }

    @Async
    public void registrar(String ref) {
        String fuente = normalizar(ref);
        visitaRepository.save(new Visita(fuente));
    }

    public Map<String, Object> obtenerStats() {
        LocalDateTime hoy   = LocalDate.now().atStartOfDay();
        LocalDateTime semana = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime mes   = LocalDate.now().minusDays(30).atStartOfDay();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("visitasHoy",    visitaRepository.countByFechaAfter(hoy));
        stats.put("visitasSemana", visitaRepository.countByFechaAfter(semana));
        stats.put("visitasMes",    visitaRepository.countByFechaAfter(mes));
        stats.put("porFuenteHoy",    porFuente(hoy));
        stats.put("porFuenteSemana", porFuente(semana));
        return stats;
    }

    public Map<String, Object> obtenerGrafica(int dias) {
        LocalDateTime desde = LocalDate.now().minusDays(dias - 1).atStartOfDay();
        List<Object[]> rows = visitaRepository.visitasPorDia(desde);

        Map<LocalDate, Long> porDia = new LinkedHashMap<>();
        for (int i = dias - 1; i >= 0; i--) {
            porDia.put(LocalDate.now().minusDays(i), 0L);
        }
        for (Object[] row : rows) {
            LocalDate dia = ((java.sql.Date) row[0]).toLocalDate();
            porDia.put(dia, ((Number) row[1]).longValue());
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        List<String> etiquetas = new ArrayList<>();
        List<Long> valores     = new ArrayList<>();
        for (Map.Entry<LocalDate, Long> e : porDia.entrySet()) {
            etiquetas.add(e.getKey().format(fmt));
            valores.add(e.getValue());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("etiquetas", etiquetas);
        result.put("valores",   valores);
        return result;
    }

    private Map<String, Long> porFuente(LocalDateTime desde) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : visitaRepository.contarPorFuenteDesde(desde)) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    private String normalizar(String ref) {
        if (ref == null || ref.isBlank()) return "directo";
        String r = ref.trim().toLowerCase();
        return FUENTES_CONOCIDAS.contains(r) ? r : "otro";
    }
}
