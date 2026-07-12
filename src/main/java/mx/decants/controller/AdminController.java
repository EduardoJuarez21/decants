package mx.decants.controller;

import mx.decants.entity.Cupon;
import mx.decants.entity.Pedido;
import mx.decants.service.ConfiguracionService;
import mx.decants.service.CuponService;
import mx.decants.service.PedidoService;
import mx.decants.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/aura-gestion")
public class AdminController {

    private final PedidoService pedidoService;
    private final ProductoService productoService;
    private final CuponService cuponService;
    private final ConfiguracionService configuracionService;

    public AdminController(PedidoService pedidoService, ProductoService productoService,
                           CuponService cuponService, ConfiguracionService configuracionService) {
        this.pedidoService = pedidoService;
        this.productoService = productoService;
        this.cuponService = cuponService;
        this.configuracionService = configuracionService;
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

    @GetMapping("/pedidos/nuevo")
    public String nuevoPedidoForm() {
        return "admin/pedido-nuevo";
    }

    @PostMapping("/pedidos/nuevo")
    public String guardarPedidoManual(@RequestParam String nombre,
                                      @RequestParam String telefono,
                                      @RequestParam(required = false) String email,
                                      @RequestParam String productos,
                                      @RequestParam Integer total,
                                      @RequestParam(required = false) String direccion,
                                      @RequestParam(required = false) String comentarios,
                                      @RequestParam(defaultValue = "CONFIRMADO") String estado,
                                      RedirectAttributes ra) {
        Pedido p = pedidoService.crearPedidoManual(nombre, telefono, email, productos, total, direccion, comentarios, estado);
        ra.addFlashAttribute("mensaje", "Pedido #" + p.getId() + " registrado correctamente.");
        return "redirect:/aura-gestion/pedidos";
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

    @GetMapping("/productos/{id}/editar")
    public String editarProducto(@PathVariable Long id, Model model) {
        return productoService.buscarPorId(id)
                .map(p -> { model.addAttribute("producto", p); return "admin/producto-editar"; })
                .orElse("redirect:/aura-gestion/productos");
    }

    @PostMapping("/productos/{id}/editar")
    public String guardarProducto(@PathVariable Long id,
                                  @RequestParam String nombre,
                                  @RequestParam String marca,
                                  @RequestParam Integer precio,
                                  @RequestParam(required = false) Integer precio5ml,
                                  @RequestParam(defaultValue = "false") boolean bestSeller,
                                  RedirectAttributes ra) {
        productoService.actualizar(id, precio, precio5ml, nombre, marca, bestSeller);
        ra.addFlashAttribute("mensaje", "Producto actualizado correctamente.");
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

    // ── Configuración ─────────────────────────────────────────────────────────

    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        model.addAttribute("stripeModo", configuracionService.getStripeModo());
        return "admin/configuracion";
    }

    @PostMapping("/configuracion/stripe-modo")
    public String cambiarStripeModo(@RequestParam String modo, RedirectAttributes ra) {
        if ("test".equals(modo) || "live".equals(modo)) {
            configuracionService.setStripeModo(modo);
            ra.addFlashAttribute("mensaje", "Modo Stripe cambiado a: " + modo.toUpperCase());
        }
        return "redirect:/aura-gestion/configuracion";
    }
}
