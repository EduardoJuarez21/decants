package mx.decants.controller;

import mx.decants.service.CuponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cupones")
public class CuponApiController {

    private final CuponService cuponService;

    public CuponApiController(CuponService cuponService) {
        this.cuponService = cuponService;
    }

    @GetMapping("/validar")
    public ResponseEntity<?> validar(@RequestParam String codigo) {
        return cuponService.validar(codigo)
                .map(c -> {
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("descuento",       c.getDescuentoPorcentaje());
                    resp.put("descripcion",     c.getDescripcion() != null ? c.getDescripcion() : "");
                    resp.put("montoMinimo",     c.getMontoMinimo());
                    resp.put("descuentoMaximo", c.getDescuentoMaximo());
                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
