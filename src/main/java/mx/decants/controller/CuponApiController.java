package mx.decants.controller;

import mx.decants.service.CuponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                .map(c -> ResponseEntity.ok(Map.of(
                        "descuento",    c.getDescuentoPorcentaje(),
                        "descripcion",  c.getDescripcion() != null ? c.getDescripcion() : ""
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
