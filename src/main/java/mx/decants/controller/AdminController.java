package mx.decants.controller;

import mx.decants.entity.Cupon;
import mx.decants.entity.Pedido;
import mx.decants.entity.Producto;
import mx.decants.entity.Vendedor;
import mx.decants.service.ConfiguracionService;
import mx.decants.service.CuponService;
import mx.decants.service.ImagenService;
import mx.decants.service.PedidoService;
import mx.decants.service.ProductoService;
import mx.decants.service.ResenaService;
import mx.decants.service.VendedorService;
import mx.decants.service.VisitaService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
    private final ResenaService resenaService;
    private final VendedorService vendedorService;

    public AdminController(PedidoService pedidoService, ProductoService productoService,
                           CuponService cuponService, ConfiguracionService configuracionService,
                           VisitaService visitaService, ImagenService imagenService,
                           ResenaService resenaService, VendedorService vendedorService) {
        this.pedidoService = pedidoService;
        this.productoService = productoService;
        this.cuponService = cuponService;
        this.configuracionService = configuracionService;
        this.visitaService = visitaService;
        this.imagenService = imagenService;
        this.resenaService = resenaService;
        this.vendedorService = vendedorService;
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

    @GetMapping("/productos/exportar-ia")
    public ResponseEntity<byte[]> exportarParaIA() throws java.io.IOException {
        List<Producto> productos = productoService.activosTodos();
        Map<Long, String> archivoPorProducto = new LinkedHashMap<>();

        var buffer = new java.io.ByteArrayOutputStream();
        try (var zip = new java.util.zip.ZipOutputStream(buffer)) {
            int numero = 1;
            for (Producto p : productos) {
                var imagen = imagenService.leerImagen(p.getImagenPrincipal());
                if (imagen.isEmpty()) {
                    continue;
                }
                String ext = extension(p.getImagenPrincipal());
                String nombreArchivo = String.format("%02d%s", numero++, ext);
                archivoPorProducto.put(p.getId(), nombreArchivo);

                zip.putNextEntry(new java.util.zip.ZipEntry("imagenes/" + nombreArchivo));
                zip.write(imagen.get());
                zip.closeEntry();
            }

            byte[] csv = productoService.exportarCsvParaIA(productos, archivoPorProducto);
            zip.putNextEntry(new java.util.zip.ZipEntry("productos.csv"));
            zip.write(csv);
            zip.closeEntry();
        }

        String filename = "catalogo-para-ia-" + LocalDate.now() + ".zip";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("application/zip"))
            .body(buffer.toByteArray());
    }

    private String extension(String path) {
        if (path == null) return "";
        int dot = path.lastIndexOf('.');
        return dot >= 0 ? path.substring(dot) : "";
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
        cargarProductosYClientes(model);
        model.addAttribute("mapsApiKey", mapsApiKey);
        return "admin/pedido-nuevo";
    }

    private void cargarProductosYClientes(Model model) {
        model.addAttribute("vendedoresActivos", vendedorService.listarActivos());

        List<Map<String, Object>> prods = productoService.listarTodos().stream()
            .filter(Producto::isActivo)
            .map(p -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", p.getId());
                m.put("nombre", p.getNombre());
                m.put("marca", p.getMarca());
                m.put("precio", p.getPrecioConDescuento() != null ? p.getPrecioConDescuento() : p.getPrecio());
                m.put("precio5ml", p.getPrecio5ml() != null
                        ? (p.getPrecio5mlConDescuento() != null ? p.getPrecio5mlConDescuento() : p.getPrecio5ml())
                        : null);
                m.put("precio3ml", p.getPrecio3ml() != null
                        ? (p.getPrecio3mlConDescuento() != null ? p.getPrecio3mlConDescuento() : p.getPrecio3ml())
                        : null);
                m.put("precioBotella", p.getPrecioBotella());
                m.put("mlBotella", p.getMlBotella());
                m.put("promoActivo", p.isPromoActivo() && p.getDescuentoPorcentaje() != null);
                return m;
            })
            .collect(Collectors.toList());
        model.addAttribute("productosJson", prods);

        List<Map<String, Object>> clientes = pedidoService.listarClientes().stream()
            .map(c -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", c.getId());
                m.put("nombre", c.getNombre());
                m.put("telefono", c.getTelefono());
                m.put("email", c.getEmail());
                m.put("ultimaDireccion", c.getUltimaDireccion());
                m.put("latitud", c.getLatitud());
                m.put("longitud", c.getLongitud());
                return m;
            })
            .collect(Collectors.toList());
        model.addAttribute("clientesJson", clientes);
    }

    @PostMapping("/pedidos/nuevo")
    public String guardarPedidoManual(@RequestParam String nombre,
                                      @RequestParam String telefono,
                                      @RequestParam(required = false) String email,
                                      @RequestParam String itemsJson,
                                      @RequestParam Integer total,
                                      @RequestParam(required = false) String direccion,
                                      @RequestParam(required = false) String latitud,
                                      @RequestParam(required = false) String longitud,
                                      @RequestParam(required = false) String comentarios,
                                      @RequestParam(defaultValue = "CONFIRMADO") String estado,
                                      @RequestParam(required = false) String vendedor,
                                      @RequestParam(required = false) Integer descuentoPorcentaje,
                                      RedirectAttributes ra) {
        Pedido p;
        try {
            p = pedidoService.crearPedidoManual(nombre, telefono, email, itemsJson, total,
                                                direccion, latitud, longitud, comentarios, estado, vendedor,
                                                descuentoPorcentaje);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/aura-gestion/pedidos/nuevo";
        }
        ra.addFlashAttribute("mensaje", "Pedido #" + p.getId() + " registrado correctamente.");
        return "redirect:/aura-gestion/pedidos";
    }

    @GetMapping("/pedidos/{id}/editar")
    public String editarPedidoForm(@PathVariable Long id, Model model) {
        Optional<Pedido> pedidoOpt = pedidoService.buscarPorId(id);
        if (pedidoOpt.isEmpty()) {
            return "redirect:/aura-gestion/pedidos";
        }
        Pedido pedido = pedidoOpt.get();
        cargarProductosYClientes(model);

        List<Map<String, Object>> itemsExistentes = pedido.getItems().stream()
            .filter(it -> it.getProducto() != null)
            .map(it -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", it.getProducto().getId());
                m.put("nombre", it.getNombre());
                m.put("variante", it.getVariante());
                m.put("precio", it.getPrecioUnitario());
                m.put("cantidad", it.getCantidad());
                return m;
            })
            .collect(Collectors.toList());
        model.addAttribute("itemsExistentesJson", itemsExistentes);
        model.addAttribute("huboItemsSinProducto",
            pedido.getItems().stream().anyMatch(it -> it.getProducto() == null));

        int descuentoActual = 0;
        if (pedido.getCodigoCuponAplicado() != null) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("MANUAL (\\d+)%")
                .matcher(pedido.getCodigoCuponAplicado());
            if (m.matches()) descuentoActual = Integer.parseInt(m.group(1));
        }
        model.addAttribute("descuentoActual", descuentoActual);

        model.addAttribute("pedido", pedido);
        model.addAttribute("mapsApiKey", mapsApiKey);
        return "admin/pedido-editar";
    }

    @PostMapping("/pedidos/{id}/editar")
    public String guardarPedidoEditado(@PathVariable Long id,
                                       @RequestParam String nombre,
                                       @RequestParam String telefono,
                                       @RequestParam(required = false) String email,
                                       @RequestParam String itemsJson,
                                       @RequestParam Integer total,
                                       @RequestParam(required = false) String direccion,
                                       @RequestParam(required = false) String latitud,
                                       @RequestParam(required = false) String longitud,
                                       @RequestParam(required = false) String comentarios,
                                       @RequestParam(defaultValue = "CREADO") String estado,
                                       @RequestParam(required = false) String vendedor,
                                       @RequestParam(required = false) Integer descuentoPorcentaje,
                                       RedirectAttributes ra) {
        try {
            pedidoService.actualizarPedidoManual(id, nombre, telefono, email, itemsJson, total,
                    direccion, latitud, longitud, comentarios, estado, vendedor, descuentoPorcentaje);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "Error al editar el pedido: " + e.getMessage());
            return "redirect:/aura-gestion/pedidos/" + id + "/editar";
        }
        ra.addFlashAttribute("mensaje", "Pedido #" + id + " actualizado correctamente.");
        return "redirect:/aura-gestion/pedidos/" + id;
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
        model.addAttribute("vendidosPorProducto", pedidoService.mlVendidoPorProducto());
        return "admin/productos";
    }

    @GetMapping("/productos/nuevo")
    public String nuevoProductoForm(Model model) {
        int siguienteOrden = productoService.listarTodos().stream()
            .mapToInt(p -> p.getOrden() != null ? p.getOrden() : 0)
            .max().orElse(0) + 1;
        model.addAttribute("siguienteOrden", siguienteOrden);
        model.addAttribute("markupDefault", configuracionService.getMarkupDefault());
        return "admin/producto-nuevo";
    }

    @PostMapping("/productos/nuevo")
    public String crearProducto(@RequestParam String nombre,
                                @RequestParam String marca,
                                @RequestParam String categoria,
                                @RequestParam String genero,
                                @RequestParam(required = false) String familia,
                                @RequestParam(required = false) String notas,
                                @RequestParam(required = false) String caracteristicas,
                                @RequestParam(required = false) String concentracion,
                                @RequestParam Integer precio,
                                @RequestParam(required = false) Integer precio5ml,
                                @RequestParam(defaultValue = "false") boolean bestSeller,
                                @RequestParam int orden,
                                @RequestParam(required = false) String proveedor,
                                @RequestParam(required = false) Double costoPorMl,
                                @RequestParam(required = false) Double markup,
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
                familia, notas, caracteristicas, precio, precio5ml, bestSeller, pathPrincipal, pathCar, orden,
                proveedor, costoPorMl, markup, concentracion);

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
                                  @RequestParam(required = false) String caracteristicas,
                                  @RequestParam(required = false) String inspiracion,
                                  @RequestParam(defaultValue = "false") boolean promoActivo,
                                  @RequestParam(required = false) Integer descuentoPorcentaje,
                                  @RequestParam(required = false) String proveedor,
                                  @RequestParam(required = false) Double costoPorMl,
                                  @RequestParam(required = false) Double markup,
                                  @RequestParam(required = false) String concentracion,
                                  @RequestParam(required = false) Integer precioBotella,
                                  @RequestParam(required = false) Integer mlBotella,
                                  @RequestParam(required = false) Integer stockBotella,
                                  @RequestParam(required = false) Double comisionFamiliar,
                                  @RequestParam(required = false) Double comisionFamiliar5ml,
                                  @RequestParam(required = false) Double comisionFamiliar3ml,
                                  RedirectAttributes ra) {
        productoService.actualizar(id, precio, precio5ml, precio3ml, nombre, marca, bestSeller, caracteristicas, inspiracion, promoActivo, descuentoPorcentaje, proveedor, costoPorMl, markup, concentracion, precioBotella, mlBotella, comisionFamiliar, comisionFamiliar5ml, comisionFamiliar3ml);
        productoService.actualizarStock(id, stock);
        productoService.actualizarStockBotella(id, stockBotella);
        ra.addFlashAttribute("mensaje", "Producto actualizado correctamente.");
        return "redirect:/aura-gestion/productos";
    }

    @PostMapping("/productos/{id}/toggle-promo")
    public String togglePromoProducto(@PathVariable Long id) {
        productoService.togglePromo(id);
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

    @PostMapping("/productos/{id}/stock-botella")
    public String actualizarStockBotella(@PathVariable Long id,
                                          @RequestParam(required = false) Integer stockBotella,
                                          RedirectAttributes ra) {
        productoService.actualizarStockBotella(id, stockBotella);
        ra.addFlashAttribute("mensaje", "Stock de frasco actualizado.");
        return "redirect:/aura-gestion/productos";
    }

    // ── Comisiones (vista para vendedoras) ───────────────────────────────────────

    @GetMapping("/comisiones")
    public String comisiones(@RequestParam(value = "mes", required = false) String mesParam,
                              @RequestParam(value = "vendedor", required = false) String vendedorParam,
                              Authentication authentication, Model model) {
        boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("esEjuarez", authentication != null && "ejuarez".equalsIgnoreCase(authentication.getName()));

        List<Vendedor> vendedoresActivos = vendedorService.listarActivos();
        model.addAttribute("vendedores", vendedoresActivos);

        Optional<Vendedor> vendedorActual;
        if (esAdmin) {
            vendedorActual = vendedorParam != null
                ? vendedorService.buscarPorUsuario(vendedorParam)
                : Optional.empty();
            if (vendedorActual.isEmpty()) {
                vendedorActual = vendedoresActivos.stream().findFirst();
            }
        } else {
            vendedorActual = vendedorService.buscarPorUsuario(authentication.getName());
        }

        if (vendedorActual.isEmpty()) {
            model.addAttribute("sinVendedoras", true);
            return "admin/comisiones";
        }
        Vendedor vendedorEntity = vendedorActual.get();
        String vendedor = vendedorEntity.getUsuario();
        model.addAttribute("vendedor", vendedor);
        model.addAttribute("vendedorEtiqueta", vendedorEntity.getNombre());
        model.addAttribute("comisionPorcentaje", vendedorEntity.getComisionPorcentaje());

        var productos = productoService.listarTodos().stream()
            .filter(Producto::isActivo)
            .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
            .collect(Collectors.toList());
        model.addAttribute("productos", productos);

        int meta = vendedorEntity.getMetaMonto();
        int llevas = pedidoService.ventasVendedorMesActual(vendedor);
        model.addAttribute("metaMonto", meta);
        model.addAttribute("metaPremio", vendedorEntity.getMetaPremio());
        model.addAttribute("metaLlevas", llevas);
        model.addAttribute("metaPorcentaje", Math.min(100, meta > 0 ? (llevas * 100 / meta) : 0));
        model.addAttribute("metaCumplida", llevas >= meta);

        YearMonth mes;
        try {
            mes = mesParam != null ? YearMonth.parse(mesParam) : YearMonth.now();
        } catch (Exception e) {
            mes = YearMonth.now();
        }
        Map<String, Object> comision = pedidoService.comisionVendedor(vendedor, mes, vendedorEntity.getComisionPorcentaje());
        model.addAttribute("comisionMes", mes.toString());
        model.addAttribute("comisionMesAnterior", mes.minusMonths(1).toString());
        model.addAttribute("comisionMesSiguiente", mes.plusMonths(1).toString());
        model.addAttribute("comisionMesEtiqueta", capitalizar(mes.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "MX"))) + " " + mes.getYear());
        model.addAttribute("comisionEsMesActual", mes.equals(YearMonth.now()));
        model.addAttribute("comisionVentasTotales", comision.get("ventasTotales"));
        model.addAttribute("comisionTotal", comision.get("comisionTotal"));
        model.addAttribute("comisionCostoTotal", comision.get("costoTotal"));
        model.addAttribute("comisionGanancia", comision.get("ganancia"));
        model.addAttribute("comisionProductosSinCosto", comision.get("productosSinCosto"));
        model.addAttribute("comisionHuboItemsSinProducto", comision.get("huboItemsSinProducto"));
        model.addAttribute("comisionDetalle", comision.get("detalle"));

        return "admin/comisiones";
    }

    private static String capitalizar(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
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

    // ── Reseñas ───────────────────────────────────────────────────────────────

    @GetMapping("/resenas")
    public String listarResenas(Model model) {
        model.addAttribute("resenas", resenaService.listarTodas());
        return "admin/resenas";
    }

    @PostMapping("/resenas/{id}/aprobar")
    public String aprobarResena(@PathVariable Long id, RedirectAttributes ra) {
        resenaService.aprobar(id);
        ra.addFlashAttribute("mensaje", "Reseña aprobada y publicada.");
        return "redirect:/aura-gestion/resenas";
    }

    @PostMapping("/resenas/{id}/eliminar")
    public String eliminarResena(@PathVariable Long id, RedirectAttributes ra) {
        resenaService.eliminar(id);
        ra.addFlashAttribute("mensaje", "Reseña eliminada.");
        return "redirect:/aura-gestion/resenas";
    }

    // ── Visitas ───────────────────────────────────────────────────────────────

    @GetMapping("/visitas")
    public String visitas(Model model) {
        visitaService.obtenerStats().forEach(model::addAttribute);
        visitaService.obtenerGrafica(30).forEach(model::addAttribute);
        return "admin/visitas";
    }

    // ── Vendedoras ────────────────────────────────────────────────────────────

    @GetMapping("/vendedores")
    public String listarVendedores(Model model) {
        model.addAttribute("vendedores", vendedorService.listarTodos());
        return "admin/vendedores";
    }

    @PostMapping("/vendedores")
    public String crearVendedor(@RequestParam String usuario, @RequestParam String password,
                                 @RequestParam String nombre, @RequestParam int metaMonto,
                                 @RequestParam(required = false) String metaPremio,
                                 @RequestParam double comisionPorcentaje,
                                 RedirectAttributes ra) {
        if (vendedorService.buscarPorUsuario(usuario).isPresent()) {
            ra.addFlashAttribute("error", "Ya existe una vendedora con ese usuario.");
            return "redirect:/aura-gestion/vendedores";
        }
        vendedorService.crear(usuario, password, nombre, metaMonto, metaPremio != null ? metaPremio.trim() : "", comisionPorcentaje);
        ra.addFlashAttribute("mensaje", "Vendedora agregada.");
        return "redirect:/aura-gestion/vendedores";
    }

    @PostMapping("/vendedores/{id}/editar")
    public String editarVendedor(@PathVariable Long id, @RequestParam String nombre,
                                  @RequestParam int metaMonto, @RequestParam(required = false) String metaPremio,
                                  @RequestParam double comisionPorcentaje,
                                  RedirectAttributes ra) {
        vendedorService.actualizar(id, nombre, metaMonto, metaPremio != null ? metaPremio.trim() : "", comisionPorcentaje);
        ra.addFlashAttribute("mensaje", "Vendedora actualizada.");
        return "redirect:/aura-gestion/vendedores";
    }

    @PostMapping("/vendedores/{id}/password")
    public String cambiarPasswordVendedor(@PathVariable Long id, @RequestParam String password,
                                           RedirectAttributes ra) {
        vendedorService.cambiarPassword(id, password);
        ra.addFlashAttribute("mensaje", "Contraseña actualizada.");
        return "redirect:/aura-gestion/vendedores";
    }

    @PostMapping("/vendedores/{id}/toggle")
    public String toggleVendedor(@PathVariable Long id, RedirectAttributes ra) {
        vendedorService.toggleActivo(id);
        return "redirect:/aura-gestion/vendedores";
    }

    // ── Configuración ─────────────────────────────────────────────────────────

    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        model.addAttribute("stripeModo",        configuracionService.getStripeModo());
        model.addAttribute("costoEnvio",        configuracionService.getCostoEnvio());
        model.addAttribute("umbralEnvioGratis", configuracionService.getUmbralEnvioGratis());
        model.addAttribute("textoEnvioLocal",   configuracionService.getTextoEnvioLocal());
        model.addAttribute("waNumero",            configuracionService.getWhatsappNegocio());
        model.addAttribute("promoTexto",          configuracionService.getPromoTexto());
        model.addAttribute("telegramToken",       configuracionService.get("telegram_bot_token", ""));
        model.addAttribute("telegramChatId",      configuracionService.get("telegram_chat_id", ""));
        model.addAttribute("emailUsername",       configuracionService.get("email_username", ""));
        model.addAttribute("emailSmtpHost",       configuracionService.get("email_smtp_host", "smtp.gmail.com"));
        model.addAttribute("emailSmtpPort",       configuracionService.get("email_smtp_port", "587"));
        model.addAttribute("emailFrom",           configuracionService.get("email_from", ""));
        model.addAttribute("markupDefault",       configuracionService.getMarkupDefault());
        return "admin/configuracion";
    }

    @PostMapping("/configuracion/markup")
    public String guardarMarkupDefault(@RequestParam double markupDefault, RedirectAttributes ra) {
        configuracionService.setMarkupDefault(markupDefault);
        ra.addFlashAttribute("mensaje", "Markup por defecto actualizado.");
        return "redirect:/aura-gestion/configuracion";
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

    @PostMapping("/configuracion/promo")
    public String guardarPromoTexto(@RequestParam(required = false) String promoTexto, RedirectAttributes ra) {
        configuracionService.set("promo_texto", promoTexto != null ? promoTexto.trim() : "");
        ra.addFlashAttribute("mensaje", "Banner de promoción actualizado.");
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