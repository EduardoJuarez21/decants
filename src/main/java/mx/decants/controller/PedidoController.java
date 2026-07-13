package mx.decants.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.validation.Valid;
import mx.decants.dto.PedidoDTO;
import mx.decants.entity.Pedido;
import mx.decants.service.CuponService;
import mx.decants.service.PedidoService;
import mx.decants.service.StripeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/pedido")
public class PedidoController {

    @Value("${google.maps.api-key:}")
    private String googleMapsKey;

    private final PedidoService pedidoService;
    private final StripeService stripeService;
    private final CuponService cuponService;

    public PedidoController(PedidoService pedidoService, StripeService stripeService, CuponService cuponService) {
        this.pedidoService = pedidoService;
        this.stripeService = stripeService;
        this.cuponService = cuponService;
    }

    @ModelAttribute("googleMapsKey")
    public String googleMapsKey() {
        return googleMapsKey;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(
            Model model,
            @RequestParam(value = "producto", required = false) String producto,
            @RequestParam(value = "entrega", required = false, defaultValue = "nacional") String entrega) {
        PedidoDTO dto = new PedidoDTO();
        if (producto != null) dto.setTipoProducto(producto);
        dto.setTipoEntrega("local".equals(entrega) ? "local" : "nacional");
        model.addAttribute("pedidoDTO", dto);
        return "pedido/form";
    }

    @PostMapping("/crear")
    public String crearPedido(
            @Valid @ModelAttribute("pedidoDTO") PedidoDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "pedido/form";
        }

        // Dirección obligatoria solo para envío nacional
        boolean esLocal = "local".equalsIgnoreCase(dto.getTipoEntrega());
        if (!esLocal && (dto.getDireccion() == null || dto.getDireccion().isBlank())) {
            result.rejectValue("direccion", "NotBlank", "La dirección de entrega es obligatoria");
            return "pedido/form";
        }

        Pedido pedido;
        try {
            pedido = pedidoService.crearPedido(dto);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Carrito inválido. Regresa y vuelve a seleccionar tus productos.");
            return "redirect:/pedido/nuevo";
        }

        // Entrega local: sin Stripe, pago contra entrega
        if ("local".equalsIgnoreCase(dto.getTipoEntrega())) {
            redirectAttributes.addFlashAttribute("pedido", pedido);
            redirectAttributes.addFlashAttribute("pagado", false);
            redirectAttributes.addFlashAttribute("entregaLocal", true);
            return "redirect:/pedido/confirmacion";
        }

        try {
            Session session = stripeService.crearCheckoutSession(pedido);
            pedidoService.actualizarStripeSession(pedido.getId(), session.getId());
            return "redirect:" + session.getUrl();
        } catch (StripeException e) {
            pedidoService.cancelarPedido(pedido.getId());
            redirectAttributes.addFlashAttribute("error", "Error al procesar el pago. Inténtalo de nuevo.");
            return "redirect:/pedido/nuevo";
        }
    }

    @GetMapping("/cupon")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validarCupon(@RequestParam String codigo) {
        return cuponService.validar(codigo)
                .map(c -> {
                    Map<String, Object> res = new java.util.HashMap<>();
                    res.put("valido", true);
                    res.put("descuento", c.getDescuentoPorcentaje());
                    res.put("descripcion", c.getDescripcion() != null ? c.getDescripcion() : "");
                    return ResponseEntity.<Map<String, Object>>ok(res);
                })
                .orElseGet(() -> {
                    Map<String, Object> res = new java.util.HashMap<>();
                    res.put("valido", false);
                    return ResponseEntity.<Map<String, Object>>ok(res);
                });
    }

    @GetMapping("/confirmacion")
    public String confirmacion(
            @RequestParam(name = "session_id", required = false) String sessionId,
            Model model) {

        if (sessionId != null) {
            try {
                boolean pagado = stripeService.verificarPago(sessionId);
                if (pagado) {
                    Pedido pedido = pedidoService.confirmarPorSession(sessionId);
                    model.addAttribute("pedido", pedido);
                    model.addAttribute("pagado", true);
                    return "pedido/confirmacion";
                }
            } catch (StripeException | IllegalArgumentException e) {
                // sesión inválida o no encontrada
            }
            return "redirect:/pedido/nuevo";
        }

        if (!model.containsAttribute("pedido")) {
            return "redirect:/pedido/nuevo";
        }
        return "pedido/confirmacion";
    }
}
