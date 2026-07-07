package mx.decants.controller;

import jakarta.validation.Valid;
import mx.decants.dto.PedidoDTO;
import mx.decants.entity.Pedido;
import mx.decants.service.PedidoService;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pedido")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // Convierte campos vacíos a null para que @Email no rechace el correo en blanco
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(
            Model model,
            @RequestParam(value = "producto", required = false) String producto) {
        PedidoDTO dto = new PedidoDTO();
        if (producto != null) {
            dto.setTipoProducto(producto);
        }
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

        Pedido pedido = pedidoService.crearPedido(dto);
        redirectAttributes.addFlashAttribute("pedido", pedido);
        return "redirect:/pedido/confirmacion";
    }

    @GetMapping("/confirmacion")
    public String confirmacion(Model model) {
        if (!model.containsAttribute("pedido")) {
            return "redirect:/pedido/nuevo";
        }
        return "pedido/confirmacion";
    }
}
