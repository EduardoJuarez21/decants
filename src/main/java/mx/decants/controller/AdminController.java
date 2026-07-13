package mx.decants.controller;

import mx.decants.entity.Cupon;
import mx.decants.entity.Pedido;
import mx.decants.entity.Producto;
import mx.decants.service.ConfiguracionService;
import mx.decants.service.CuponService;
import mx.decants.service.PedidoService;
import mx.decants.service.ProductoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/aura-gestion")
public class AdminController {

    @Value("${google.maps.api-key:}")
    private String mapsApiKey;

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
    public String nuevoPedidoForm(Model model) {
        List<Map<String, Object>> prods = productoService.listarTodos().stream()
            .filter(Producto::isActivo)
            .map(p -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", p.getId());
                m.put("nombre", p.getNombre());
                m.put("marca", p.getMarca());
                m.put("precio", p.getPrecio());
                m.put("precio5ml", p.getPrecio5ml());
                return m;
            })
            .collect(Collectors.toList());
        model.addAttribute("productosJson", prods);
        model.addAttribute("mapsApiKey", mapsApiKey);
        return "admin/pedido-nuevo";
    }

    @PostMapping("/pedidos/nuevo")
    public String guardarPedidoManual(@RequestParam String nombre,
                                      @RequestParam String telefono,
                                      @RequestParam(required = false) String email,
                                      @RequestParam String productos,
                                      @RequestParam Integer total,
                                      @RequestParam(required = false) String direccion,
                                      @RequestParam(required = false) String latitud,
                                      @RequestParam(required = false) String longitud,
                                      @RequestParam(required = false) String comentarios,
                                      @RequestParam(defaultValue = "CONFIRMADO") String estado,
                                      RedirectAttributes ra) {
        Pedido p = pedidoService.crearPedidoManual(nombre, telefono, email, productos, total,
                                                    direccion, latitud, longitud, comentarios, estado);
        ra.addFlashAttribute("mensaje", "Pedido #" + p.getId() + " registrado correctamente.");
        return "redirect:/aura-gestion/pedidos";
    }

    @PostMapping("/pedidos/{id}/guia")
    public String actualizarGuia(@PathVariable Long id,
                                 @RequestParam(required = false) String guia,
                                 RedirectAttributes ra) {
        pedidoService.actualizarGuia(id, guia);
        ra.addFlashAttribute("mensaje", "Número de guía actualizado.");
        return "redirect:/aura-gestion/pedidos/" + id;
    }

    @PostMapping("/pedidos/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam String estado,
                                RedirectAttributes ra) {
        pedidoService.cambiarEstado(id, estado);
        ra.addFlashAttribute("mensaje", "Estado actualizado correctamente.");
        return "redirect:/aura-gestion/pedidos/" + id;
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
        model.addAttribute("stripeModo",        configuracionService.getStripeModo());
        model.addAttribute("costoEnvio",        configuracionService.getCostoEnvio());
        model.addAttribute("umbralEnvioGratis", configuracionService.getUmbralEnvioGratis());
        model.addAttribute("textoEnvioLocal",   configuracionService.getTextoEnvioLocal());
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

    @PostMapping("/configuracion/envio")
    public String guardarEnvioConfig(@RequestParam int costoEnvio,
                                      @RequestParam int umbralGratis,
                                      @RequestParam String textoEnvioLocal,
                                      RedirectAttributes ra) {
        configuracionService.set("envio_costo",         String.valueOf(costoEnvio));
        configuracionService.set("envio_umbral_gratis", String.valueOf(umbralGratis));
        configuracionService.set("envio_texto_local",   textoEnvioLocal.trim());
        ra.addFlashAttribute("mensaje", "Configuración de envío actualizada.");
        return "redirect:/aura-gestion/configuracion";
    }
}