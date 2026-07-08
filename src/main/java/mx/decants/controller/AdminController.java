package mx.decants.controller;

import mx.decants.entity.Pedido;
import mx.decants.service.PedidoService;
import mx.decants.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PedidoService pedidoService;
    private final ProductoService productoService;

    public AdminController(PedidoService pedidoService, ProductoService productoService) {
        this.pedidoService = pedidoService;
        this.productoService = productoService;
    }

    // ── Pedidos ──────────────────────────────────────────────────────────────

    @GetMapping("/pedidos")
    public String listarPedidos(Model model) {
        List<Pedido> pedidos = pedidoService.listarPedidos();
        model.addAttribute("pedidos", pedidos);
        return "admin/pedidos";
    }

    @GetMapping("/pedidos/{id}")
    public String detallePedido(@PathVariable Long id, Model model) {
        Optional<Pedido> pedido = pedidoService.buscarPorId(id);
        if (pedido.isEmpty()) {
            return "redirect:/admin/pedidos";
        }
        model.addAttribute("pedido", pedido.get());
        return "admin/detalle";
    }

    // ── Productos ─────────────────────────────────────────────────────────────

    @GetMapping("/productos")
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        return "admin/productos";
    }

    @PostMapping("/productos/{id}/toggle")
    public String toggleProducto(@PathVariable Long id) {
        productoService.toggleActivo(id);
        return "redirect:/admin/productos";
    }
}
