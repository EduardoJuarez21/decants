package mx.decants.controller;

import mx.decants.service.CodigoPostalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/codigo-postal")
public class CodigoPostalApiController {

    private final CodigoPostalService codigoPostalService;

    public CodigoPostalApiController(CodigoPostalService codigoPostalService) {
        this.codigoPostalService = codigoPostalService;
    }

    @GetMapping("/{cp}")
    public ResponseEntity<Map<String, Object>> buscar(@PathVariable String cp) {
        return ResponseEntity.ok(codigoPostalService.buscar(cp));
    }
}
