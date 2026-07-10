package mx.decants.controller;

import mx.decants.entity.Cupon;
import mx.decants.entity.Pedido;
import mx.decants.service.CuponService;
import mx.decants.service.PedidoService;
import mx.decants.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/aura-gestion")
public class AdminController {

    private final PedidoService pedidoService;
    private final ProductoService productoService;
    private final CuponService cuponService;

    public AdminController(PedidoService pedidoService, ProductoService productoService, CuponService cuponService) {
        this.pedidoService = pedidoService;
        this.productoService = productoService;
        this.cuponService = cuponService;
    }

    // ── Login ────────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String login() {
        return "admin/login";
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
            return "redirect:/aura-gestion/pedidos";
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
        return "redirect:/aura-gestion/productos";
    }

    // ── Clientes ──────────────────────────────────────────────────────────────

    @GetMapping("/clientes")
    public String listarClientes(Model model) {
        model.addAttribute("clientes", pedidoService.listarClientes());
        return "admin/clientes";
    }

    @GetMapping("/clientes/{id}")
    public String detalleCliente(@PathVariable Long id, Model model) {
        return pedidoService.buscarClientePorId(id)
                .map(c -> { model.addAttribute("cliente", c); return "admin/cliente-detalle"; })
                .orElse("redirect:/aura-gestion/clientes");
    }

    // ── Cupones ───────────────────────────────────────────────────────────────

    @GetMapping("/cupones")
    public String listarCupones(Model model) {
        model.addAttribute("cupones", cuponService.listarTodos());
        model.addAttribute("nuevoCupon", new Cupon());
        return "admin/cupones";
    }

    @PostMapping("/cupones")
    public String crearCupon(@ModelAttribute Cupon cupon) {
        cuponService.guardar(cupon);
        return "redirect:/aura-gestion/cupones";
    }

    @PostMapping("/cupones/{id}/toggle")
    public String toggleCupon(@PathVariable Long id) {
        cuponService.toggleActivo(id);
        return "redirect:/aura-gestion/cupones";
    }

    @PostMapping("/cupones/{id}/eliminar")
    public String eliminarCupon(@PathVariable Long id) {
        cuponService.eliminar(id);
        return "redirect:/aura-gestion/cupones";
    }
}
