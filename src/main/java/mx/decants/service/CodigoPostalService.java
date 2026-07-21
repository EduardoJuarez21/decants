package mx.decants.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CodigoPostalService {

    private static final Logger log = LoggerFactory.getLogger(CodigoPostalService.class);
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(4))
        .build();

    private final ObjectMapper objectMapper;
    private final Map<String, Map<String, Object>> cache = new ConcurrentHashMap<>();

    public CodigoPostalService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> buscar(String cp) {
        if (cp == null || !cp.matches("\\d{5}")) {
            return Map.of("ok", false, "error", "Código postal inválido.");
        }

        Map<String, Object> cached = cache.get(cp);
        if (cached != null) return cached;

        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://cp.terio.dev/v1/codigos-postales/" + cp))
                .timeout(Duration.ofSeconds(4))
                .GET()
                .build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                return Map.of("ok", false, "error", "No se pudo verificar el código postal.");
            }

            JsonNode root  = objectMapper.readTree(resp.body());
            JsonNode datos = root.get("datos");
            if (datos == null || !datos.isArray() || datos.isEmpty()) {
                return Map.of("ok", false, "error", "No encontramos ese código postal.");
            }

            List<Map<String, Object>> colonias = new ArrayList<>();
            for (JsonNode n : datos) {
                Map<String, Object> c = new HashMap<>();
                c.put("nombre", n.path("asentamiento").asText());
                c.put("lat", n.path("lat").asDouble());
                c.put("lng", n.path("lng").asDouble());
                colonias.add(c);
            }

            JsonNode first = datos.get(0);
            Map<String, Object> result = new HashMap<>();
            result.put("ok", true);
            result.put("estado", first.path("estado").asText());
            result.put("municipio", first.path("municipio").asText());
            result.put("ciudad", first.path("ciudad").asText());
            result.put("colonias", colonias);

            cache.put(cp, result);
            return result;
        } catch (Exception e) {
            log.warn("Error consultando código postal {}: {}", cp, e.getMessage());
            return Map.of("ok", false, "error", "No se pudo verificar el código postal, intenta de nuevo.");
        }
    }
}
