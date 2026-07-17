package mx.decants.controller;

import mx.decants.entity.Cupon;
import mx.decants.entity.Pedido;
import mx.decants.entity.Producto;
import mx.decants.service.ConfiguracionService;
import mx.decants.service.CuponService;
import mx.decants.service.ImagenService;
import mx.decants.service.PedidoService;
import mx.decants.service.ProductoService;
import mx.decants.service.VisitaService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
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
    private final VisitaService visitaService;
    private final ImagenService imagenService;

    public AdminController(PedidoService pedidoService, ProductoService productoService,
                           CuponService cuponService, ConfiguracionService configuracionService,
                           VisitaService visitaService, ImagenService imagenService) {
        this.pedidoService = pedidoService;
        this.productoService = productoService;
        this.cuponService = cuponService;
        this.configuracionService = configuracionService;
        this.visitaService = visitaService;
        this.imagenService = imagenService;
    }

    // ── Login ────────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        Map<String, Object> stats = pedidoService.obtenerDashboard();
        stats.forEach(model::addAttribute);
        visitaService.obtenerStats().forEach(model::addAttribute);
        return "admin/dashboard";
    }

    // ── Export CSV ────────────────────────────────────────────────────────────

    @GetMapping("/pedidos/exportar")
    public ResponseEntity<byte[]> exportarCsv() {
        byte[] csv = pedidoService.exportarCsv();
        String filename = "pedidos-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csv);
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

    @GetMapping("/productos/nuevo")
    public String nuevoProductoForm(Model model) {
        int siguienteOrden = productoService.listarTodos().stream()
            .mapToInt(p -> p.getOrden() != null ? p.getOrden() : 0)
            .max().orElse(0) + 1;
        model.addAttribute("siguienteOrden", siguienteOrden);
        return "admin/producto-nuevo";
    }

    @PostMapping("/productos/nuevo")
    public String crearProducto(@RequestParam String nombre,
                                @RequestParam String marca,
                                @RequestParam String categoria,
                                @RequestParam String genero,
                                @RequestParam(required = false) String familia,
                                @RequestParam(required = false) String notas,
                                @RequestParam Integer precio,
                                @RequestParam(required = false) Integer precio5ml,
                                @RequestParam(defaultValue = "false") boolean bestSeller,
                                @RequestParam int orden,
                                @RequestParam("imagenPrincipal") MultipartFile imagenPrincipal,
                                @RequestParam(value = "imagenCaracteristicas", required = false) MultipartFile imagenCaracteristicas,
                                RedirectAttributes ra) {
        try {
            String slug = nombre.trim().toLowerCase()
                .replaceAll("[áàäâã]", "a").replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i").replaceAll("[óòöôõ]", "o")
                .replaceAll("[úùüû]", "u").replaceAll("[ñ]", "n")
                .replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");

            String pathPrincipal = imagenService.guardarYConvertir(imagenPrincipal, categoria, genero, slug);
            String pathCar = null;
            if (imagenCaracteristicas != null && !imagenCaracteristicas.isEmpty()) {
                pathCar = imagenService.guardarYConvertir(imagenCaracteristicas, categoria, genero, "car-" + slug);
            }

            productoService.crear(nombre, marca, categoria, genero,
                familia, notas, precio, precio5ml, bestSeller, pathPrincipal, pathCar, orden);

            ra.addFlashAttribute("mensaje", "Producto \"" + nombre + "\" creado correctamente.");
            return "redirect:/aura-gestion/productos";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al crear el producto: " + e.getMessage());
            return "redirect:/aura-gestion/productos/nuevo";
        }
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
                                  @RequestParam(required = false) Integer precio3ml,
                                  @RequestParam(defaultValue = "false") boolean bestSeller,
                                  @RequestParam(required = false) Integer stock,
                                  RedirectAttributes ra) {
        productoService.actualizar(id, precio, precio5ml, precio3ml, nombre, marca, bestSeller);
        productoService.actualizarStock(id, stock);
        ra.addFlashAttribute("mensaje", "Producto actualizado correctamente.");
        return "redirect:/aura-gestion/productos";
    }

    @PostMapping("/productos/{id}/stock")
    public String actualizarStock(@PathVariable Long id,
                                   @RequestParam(required = false) Integer stock,
                                   RedirectAttributes ra) {
        productoService.actualizarStock(id, stock);
        ra.addFlashAttribute("mensaje", "Stock actualizado.");
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
        model.addAttribute("waNumero",            configuracionService.getWhatsappNegocio());
        model.addAttribute("telegramToken",       configuracionService.get("telegram_bot_token", ""));
        model.addAttribute("telegramChatId",      configuracionService.get("telegram_chat_id", ""));
        model.addAttribute("emailUsername",       configuracionService.get("email_username", ""));
        model.addAttribute("emailSmtpHost",       configuracionService.get("email_smtp_host", "smtp.gmail.com"));
        model.addAttribute("emailSmtpPort",       configuracionService.get("email_smtp_port", "587"));
        model.addAttribute("emailFrom",           configuracionService.get("email_from", ""));
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

    @PostMapping("/configuracion/whatsapp")
    public String guardarWhatsapp(@RequestParam String waNumero, RedirectAttributes ra) {
        configuracionService.set("whatsapp_negocio", waNumero.trim().replaceAll("[^0-9]", ""));
        ra.addFlashAttribute("mensaje", "Número de WhatsApp actualizado.");
        return "redirect:/aura-gestion/configuracion";
    }

    @PostMapping("/configuracion/telegram")
    public String guardarTelegram(@RequestParam String telegramToken,
                                   @RequestParam String telegramChatId,
                                   RedirectAttributes ra) {
        configuracionService.set("telegram_bot_token", telegramToken.trim());
        configuracionService.set("telegram_chat_id",   telegramChatId.trim());
        ra.addFlashAttribute("mensaje", "Configuración de Telegram guardada.");
        return "redirect:/aura-gestion/configuracion";
    }

    @PostMapping("/configuracion/email")
    public String guardarEmail(@RequestParam String emailUsername,
                                @RequestParam String emailPassword,
                                @RequestParam(required = false) String emailSmtpHost,
                                @RequestParam(required = false) String emailSmtpPort,
                                @RequestParam(required = false) String emailFrom,
                                RedirectAttributes ra) {
        configuracionService.set("email_username",  emailUsername.trim());
        if (!emailPassword.isBlank()) {
            configuracionService.set("email_password", emailPassword.trim());
        }
        configuracionService.set("email_smtp_host", emailSmtpHost != null && !emailSmtpHost.isBlank() ? emailSmtpHost.trim() : "smtp.gmail.com");
        configuracionService.set("email_smtp_port", emailSmtpPort != null && !emailSmtpPort.isBlank() ? emailSmtpPort.trim() : "587");
        configuracionService.set("email_from",      emailFrom != null ? emailFrom.trim() : "");
        ra.addFlashAttribute("mensaje", "Configuración de email guardada.");
        return "redirect:/aura-gestion/configuracion";
    }
}