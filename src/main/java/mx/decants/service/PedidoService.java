package mx.decants.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.decants.dto.PedidoDTO;
import mx.decants.entity.Cliente;
import mx.decants.entity.EstadoPedido;
import mx.decants.entity.Pedido;
import mx.decants.entity.PedidoItem;
import mx.decants.entity.Producto;
import mx.decants.entity.Cupon;
import mx.decants.repository.ClienteRepository;
import mx.decants.repository.PedidoItemRepository;
import mx.decants.repository.PedidoRepository;
import mx.decants.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Service
@Transactional
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private static final Map<String, Integer> PACKAGE_PRICES = Map.of(
        "individual", 99,
        "discovery",  279,
        "coleccion",  449,
        "exclusivo",  399,
        "regalo",     299
    );

    private final PedidoRepository pedidoRepository;
    private final PedidoItemRepository pedidoItemRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final CuponService cuponService;
    private final ConfiguracionService configuracionService;
    private final TelegramService telegramService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    public PedidoService(PedidoRepository pedidoRepository,
                         PedidoItemRepository pedidoItemRepository,
                         ProductoRepository productoRepository,
                         ClienteRepository clienteRepository,
                         CuponService cuponService,
                         ConfiguracionService configuracionService,
                         TelegramService telegramService,
                         EmailService emailService,
                         ObjectMapper objectMapper) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoItemRepository = pedidoItemRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.cuponService = cuponService;
        this.configuracionService = configuracionService;
        this.telegramService = telegramService;
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    public Pedido crearPedido(PedidoDTO dto) {
        boolean esLocal = "local".equalsIgnoreCase(dto.getTipoEntrega());
        int subtotal = calcularSubtotal(dto.getCartItemsJson(), dto.getPackageType());
        int envio    = esLocal ? 0 : calcularEnvio(subtotal);
        int total    = subtotal + envio;

        Pedido pedido = new Pedido();
        pedido.setNombreCliente(dto.getNombreCliente());
        pedido.setTelefono(dto.getTelefono());
        pedido.setEmail(dto.getEmail());
        pedido.setTipoProducto(dto.getTipoProducto());
        pedido.setCantidad(dto.getCantidad());
        pedido.setTextoPersonalizado(dto.getTextoPersonalizado());
        pedido.setColorPreferido(dto.getColorPreferido());
        pedido.setFechaEvento(dto.getFechaEvento());
        pedido.setComentarios(dto.getComentarios());
        List<PedidoItem> items = buildItems(dto, pedido);
        items.forEach(it -> it.setPedido(pedido));
        pedido.setItems(items);
        pedido.setProductosSeleccionados(buildResumen(items));

        if (dto.getCodigoCupon() != null && !dto.getCodigoCupon().isBlank()) {
            cuponService.validar(dto.getCodigoCupon()).ifPresent(cupon -> {
                if (cupon.getMontoMinimo() != null && subtotal < cupon.getMontoMinimo()) return;
                int descuento = Math.round(subtotal * cupon.getDescuentoPorcentaje() / 100f);
                if (cupon.getDescuentoMaximo() != null && descuento > cupon.getDescuentoMaximo()) {
                    descuento = cupon.getDescuentoMaximo();
                }
                pedido.setCodigoCuponAplicado(cupon.getCodigo());
                pedido.setDescuentoAplicado(descuento);
            });
        }
        int descuento = pedido.getDescuentoAplicado() != null ? pedido.getDescuentoAplicado() : 0;
        pedido.setTotalPagado(total - descuento);
        if (esLocal && dto.getCodigoPostal() != null) {
            pedido.setDireccion("CP " + dto.getCodigoPostal());
        } else {
            pedido.setDireccion(dto.getDireccion());
        }
        if (dto.getLatitud() != null && !dto.getLatitud().isBlank()) {
            try { pedido.setLatitud(Double.parseDouble(dto.getLatitud())); } catch (NumberFormatException ignored) {}
        }
        if (dto.getLongitud() != null && !dto.getLongitud().isBlank()) {
            try { pedido.setLongitud(Double.parseDouble(dto.getLongitud())); } catch (NumberFormatException ignored) {}
        }
        if (esLocal) {
            pedido.setEstadoPedido(EstadoPedido.CREADO);
            pedido.setEntorno("local");
        } else {
            pedido.setEstadoPedido(EstadoPedido.PENDIENTE_PAGO);
            pedido.setEntorno(configuracionService.getStripeModo());
        }
        pedido.setCliente(encontrarOCrearCliente(dto));
        Pedido saved = pedidoRepository.save(pedido);
        log.info("Pedido #{} creado — cliente: {}, total: ${} MXN, entrega: {}, productos: {}",
                saved.getId(), dto.getNombreCliente(), saved.getTotalPagado(),
                esLocal ? "local" : "nacional", saved.getProductosSeleccionados());
        if (esLocal) {
            descontarStock(items);
            telegramService.notificarNuevoPedido(saved);
            emailService.enviarConfirmacion(saved);
        }
        return saved;
    }

    private List<PedidoItem> buildItems(PedidoDTO dto, Pedido pedido) {
        List<PedidoItem> items = new ArrayList<>();
        if (dto.getPackageType() != null && !dto.getPackageType().isBlank()) {
            PedidoItem item = new PedidoItem();
            item.setNombre(formatPaquete(dto.getPackageType()));
            item.setVariante("paquete");
            item.setCantidad(1);
            item.setPrecioUnitario(PACKAGE_PRICES.get(dto.getPackageType()));
            items.add(item);
        } else {
            try {
                JsonNode cartItems = objectMapper.readTree(dto.getCartItemsJson());
                for (JsonNode ci : cartItems) {
                    long productId = ci.get("id").asLong();
                    int qty = ci.get("qty").asInt(1);
                    String variant = ci.has("variant") ? ci.get("variant").asText() : "10ml";
                    Producto p = productoRepository.findById(productId).orElseThrow();
                    if (p.getStock() != null && p.getStock() < qty) {
                        String msg = p.getStock() == 0
                            ? p.getNombre() + " está agotado"
                            : p.getNombre() + " solo tiene " + p.getStock() + " unidad(es) disponible(s)";
                        throw new IllegalArgumentException(msg);
                    }
                    int price = "5ml".equals(variant) && p.getPrecio5ml() != null
                            ? p.getPrecio5ml() : p.getPrecio();
                    PedidoItem item = new PedidoItem();
                    item.setProducto(p);
                    item.setNombre(p.getNombre());
                    item.setVariante(variant);
                    item.setCantidad(qty);
                    item.setPrecioUnitario(price);
                    items.add(item);
                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalArgumentException("Carrito inválido");
            }
        }
        return items;
    }

    private String buildResumen(List<PedidoItem> items) {
        StringJoiner sj = new StringJoiner(", ");
        for (PedidoItem item : items) {
            String entry = item.getNombre();
            if (!"paquete".equals(item.getVariante())) entry += " " + item.getVariante();
            if (item.getCantidad() > 1) entry += " x" + item.getCantidad();
            sj.add(entry);
        }
        return sj.toString();
    }

    private String formatPaquete(String type) {
        return switch (type) {
            case "individual" -> "Paquete Individual";
            case "discovery"  -> "Discovery Set";
            case "coleccion"  -> "Colección";
            case "exclusivo"  -> "Paquete Exclusivo";
            case "regalo"     -> "Set de Regalo";
            default           -> type;
        };
    }

    private Cliente encontrarOCrearCliente(PedidoDTO dto) {
        Cliente cliente = clienteRepository.findByTelefono(dto.getTelefono())
                .orElseGet(Cliente::new);
        cliente.setTelefono(dto.getTelefono());
        cliente.setNombre(dto.getNombreCliente());
        if (dto.getEmail() != null) cliente.setEmail(dto.getEmail());
        if (dto.getDireccion() != null) cliente.setUltimaDireccion(dto.getDireccion());
        if (dto.getLatitud() != null && !dto.getLatitud().isBlank()) {
            try { cliente.setLatitud(Double.parseDouble(dto.getLatitud())); } catch (NumberFormatException ignored) {}
        }
        if (dto.getLongitud() != null && !dto.getLongitud().isBlank()) {
            try { cliente.setLongitud(Double.parseDouble(dto.getLongitud())); } catch (NumberFormatException ignored) {}
        }
        return clienteRepository.save(cliente);
    }

    private int calcularEnvio(int subtotal) {
        int umbral = configuracionService.getUmbralEnvioGratis();
        return subtotal >= umbral ? 0 : configuracionService.getCostoEnvio();
    }

    private int calcularSubtotal(String cartItemsJson, String packageType) {
        if (packageType != null && !packageType.isBlank()) {
            Integer pkgPrice = PACKAGE_PRICES.get(packageType);
            if (pkgPrice == null) {
                throw new IllegalArgumentException("Paquete inválido: " + packageType);
            }
            return pkgPrice;
        }

        if (cartItemsJson == null || cartItemsJson.isBlank()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        try {
            JsonNode items = objectMapper.readTree(cartItemsJson);
            if (!items.isArray() || items.isEmpty()) {
                throw new IllegalArgumentException("El carrito está vacío");
            }
            int total = 0;
            for (JsonNode item : items) {
                long productId = item.get("id").asLong();
                int qty = item.get("qty").asInt(1);
                String variant = item.has("variant") ? item.get("variant").asText() : "10ml";

                Producto p = productoRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productId));

                int price = "5ml".equals(variant) && p.getPrecio5ml() != null
                    ? p.getPrecio5ml()
                    : p.getPrecio();

                total += price * qty;
            }
            if (total <= 0) {
                throw new IllegalArgumentException("Total inválido");
            }
            return total;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Carrito inválido");
        }
    }

    private void descontarStock(List<PedidoItem> items) {
        if (items == null) return;
        int umbralAlerta = 2;
        for (PedidoItem item : items) {
            Producto p = item.getProducto();
            if (p != null && p.getStock() != null) {
                p.setStock(Math.max(0, p.getStock() - item.getCantidad()));
                productoRepository.save(p);
                log.info("Stock producto #{} ({}) → {}", p.getId(), p.getNombre(), p.getStock());
                if (p.getStock() <= umbralAlerta) {
                    telegramService.notificarStockBajo(p.getNombre(), p.getStock());
                }
            }
        }
    }

    public void actualizarStripeSession(Long pedidoId, String sessionId) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        pedido.setStripeSessionId(sessionId);
        pedidoRepository.save(pedido);
    }

    public void cancelarPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        pedido.setEstadoPedido(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);
        log.warn("Pedido #{} cancelado (error en Stripe)", pedidoId);
    }

    public Pedido confirmarPorSession(String sessionId) {
        Pedido pedido = pedidoRepository.findByStripeSessionId(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada: " + sessionId));
        if (pedido.getEstadoPedido() == EstadoPedido.PENDIENTE_PAGO) {
            pedido.setEstadoPedido(EstadoPedido.CONFIRMADO);
            pedidoRepository.save(pedido);
            descontarStock(pedido.getItems());
            log.info("Pedido #{} CONFIRMADO (pago recibido) — cliente: {}, total: ${} MXN",
                    pedido.getId(), pedido.getNombreCliente(), pedido.getTotalPagado());
            telegramService.notificarNuevoPedido(pedido);
            emailService.enviarConfirmacion(pedido);
        }
        return pedido;
    }

    public Pedido crearPedidoManual(String nombre, String telefono, String email,
                                     String productosDesc, Integer total,
                                     String direccion, String latitud, String longitud,
                                     String comentarios, String estadoStr) {
        Pedido pedido = new Pedido();
        pedido.setNombreCliente(nombre.trim());
        pedido.setTelefono(telefono.trim());
        pedido.setEmail(email != null && !email.isBlank() ? email.trim() : null);
        pedido.setTipoProducto("Venta directa");
        pedido.setCantidad(1);
        pedido.setProductosSeleccionados(productosDesc != null ? productosDesc.trim() : "");
        pedido.setTotalPagado(total);
        pedido.setDireccion(direccion != null && !direccion.isBlank() ? direccion.trim() : null);
        if (latitud != null && !latitud.isBlank()) {
            try { pedido.setLatitud(Double.parseDouble(latitud)); } catch (NumberFormatException ignored) {}
        }
        if (longitud != null && !longitud.isBlank()) {
            try { pedido.setLongitud(Double.parseDouble(longitud)); } catch (NumberFormatException ignored) {}
        }
        pedido.setComentarios(comentarios != null && !comentarios.isBlank() ? comentarios.trim() : null);
        pedido.setEntorno("manual");
        EstadoPedido estado = switch (estadoStr) {
            case "CONFIRMADO"       -> EstadoPedido.CONFIRMADO;
            case "LISTO_PARA_ENVIO" -> EstadoPedido.LISTO_PARA_ENVIO;
            case "ENVIADO"          -> EstadoPedido.ENVIADO;
            case "ENTREGADO"        -> EstadoPedido.ENTREGADO;
            case "CANCELADO"        -> EstadoPedido.CANCELADO;
            default                 -> EstadoPedido.CREADO;
        };
        pedido.setEstadoPedido(estado);

        Cliente cliente = clienteRepository.findByTelefono(telefono.trim()).orElseGet(Cliente::new);
        cliente.setTelefono(telefono.trim());
        cliente.setNombre(nombre.trim());
        if (email != null && !email.isBlank()) cliente.setEmail(email.trim());
        if (direccion != null && !direccion.isBlank()) cliente.setUltimaDireccion(direccion.trim());
        if (latitud != null && !latitud.isBlank()) {
            try { cliente.setLatitud(Double.parseDouble(latitud)); } catch (NumberFormatException ignored) {}
        }
        if (longitud != null && !longitud.isBlank()) {
            try { cliente.setLongitud(Double.parseDouble(longitud)); } catch (NumberFormatException ignored) {}
        }
        pedido.setCliente(clienteRepository.save(cliente));

        Pedido saved = pedidoRepository.save(pedido);
        log.info("Pedido manual #{} creado — cliente: {}, total: ${} MXN", saved.getId(), nombre, total);
        telegramService.notificarNuevoPedido(saved);
        return saved;
    }

    public void actualizarGuia(Long id, String guia) {
        pedidoRepository.findById(id).ifPresent(p -> {
            p.setNumeroGuia(guia != null && !guia.isBlank() ? guia.trim() : null);
            pedidoRepository.save(p);
            log.info("Pedido #{} → guía: {}", id, guia);
        });
    }

    public void cambiarEstado(Long id, String estadoStr) {
        pedidoRepository.findById(id).ifPresent(p -> {
            try {
                EstadoPedido nuevoEstado = EstadoPedido.valueOf(estadoStr);
                p.setEstadoPedido(nuevoEstado);
                pedidoRepository.save(p);
                log.info("Pedido #{} → estado: {}", id, estadoStr);
                if (nuevoEstado == EstadoPedido.ENVIADO) {
                    emailService.enviarNotificacionEnvio(p);
                }
            } catch (IllegalArgumentException ignored) {}
        });
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAllByOrderByFechaCreacionDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarClientes() {
        return clienteRepository.findAllWithPedidos();
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> buscarClientePorId(Long id) {
        return clienteRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> buscarClientePorTelefono(String telefono) {
        return clienteRepository.findByTelefono(telefono);
    }

    private static final java.util.regex.Pattern CODIGO_PATTERN =
        java.util.regex.Pattern.compile("^AURA-[A-Z0-9]{8}$");

    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorCodigoYTelefono(String codigo, String telefono) {
        if (codigo == null || codigo.isBlank()) return Optional.empty();
        String codigoNorm = codigo.toUpperCase().trim();
        if (!CODIGO_PATTERN.matcher(codigoNorm).matches()) return Optional.empty();
        String tel = telefono == null ? "" : telefono.replaceAll("[^0-9]", "");
        return pedidoRepository.findByCodigoPublicoAndTelefono(codigoNorm, tel);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerDashboard() {
        List<Pedido> todos = pedidoRepository.findAllByOrderByFechaCreacionDesc();

        Set<EstadoPedido> validos = Set.of(
            EstadoPedido.CREADO, EstadoPedido.CONFIRMADO,
            EstadoPedido.LISTO_PARA_ENVIO, EstadoPedido.ENVIADO, EstadoPedido.ENTREGADO
        );

        LocalDateTime hoy    = LocalDate.now().atStartOfDay();
        LocalDateTime semana = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime mes    = LocalDate.now().minusDays(30).atStartOfDay();

        List<Pedido> validosList = todos.stream().filter(p -> validos.contains(p.getEstadoPedido())).toList();

        int ventasHoy    = sumarVentas(validosList, hoy);
        int ventasSemana = sumarVentas(validosList, semana);
        int ventasMes    = sumarVentas(validosList, mes);
        long pedidosHoy    = contarDespuesDe(todos, hoy);
        long pedidosSemana = contarDespuesDe(todos, semana);
        long pedidosMes    = contarDespuesDe(todos, mes);

        Map<String, Long> porEstado = todos.stream()
            .collect(Collectors.groupingBy(p -> p.getEstadoPedido().getEtiqueta(), Collectors.counting()));

        List<Object[]> topProductos = pedidoItemRepository.findTopProductos(PageRequest.of(0, 5));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("ventasHoy",      ventasHoy);
        stats.put("ventasSemana",   ventasSemana);
        stats.put("ventasMes",      ventasMes);
        stats.put("pedidosHoy",     pedidosHoy);
        stats.put("pedidosSemana",  pedidosSemana);
        stats.put("pedidosMes",     pedidosMes);
        stats.put("totalPedidos",   todos.size());
        stats.put("porEstado",      porEstado);
        stats.put("topProductos",   topProductos);
        stats.put("ultimosPedidos", todos.stream().limit(10).toList());
        return stats;
    }

    private int sumarVentas(List<Pedido> pedidos, LocalDateTime desde) {
        return pedidos.stream()
            .filter(p -> p.getFechaCreacion() != null && p.getFechaCreacion().isAfter(desde))
            .mapToInt(p -> p.getTotalPagado() != null ? p.getTotalPagado() : 0)
            .sum();
    }

    private long contarDespuesDe(List<Pedido> pedidos, LocalDateTime desde) {
        return pedidos.stream()
            .filter(p -> p.getFechaCreacion() != null && p.getFechaCreacion().isAfter(desde))
            .count();
    }

    @Transactional(readOnly = true)
    public byte[] exportarCsv() {
        List<Pedido> pedidos = pedidoRepository.findAllByOrderByFechaCreacionDesc();
        StringBuilder sb = new StringBuilder();
        sb.append("#,Fecha,Cliente,Teléfono,Email,Productos,Total MXN,Estado,Tipo,Guía,Cupón,Dirección\n");
        for (Pedido p : pedidos) {
            sb.append(csv(p.getId()))
              .append(",").append(csv(p.getFechaCreacion() != null ? p.getFechaCreacion().toLocalDate().toString() : ""))
              .append(",").append(csv(p.getNombreCliente()))
              .append(",").append(csv(p.getTelefono()))
              .append(",").append(csv(p.getEmail()))
              .append(",").append(csv(p.getProductosSeleccionados()))
              .append(",").append(csv(p.getTotalPagado()))
              .append(",").append(csv(p.getEstadoPedido().getEtiqueta()))
              .append(",").append(csv(tipoEntrega(p.getEntorno())))
              .append(",").append(csv(p.getNumeroGuia()))
              .append(",").append(csv(p.getCodigoCuponAplicado()))
              .append(",").append(csv(p.getDireccion()))
              .append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String csv(Object val) {
        if (val == null) return "";
        String s = val.toString().replace("\"", "\"\"");
        return s.contains(",") || s.contains("\"") || s.contains("\n") ? "\"" + s + "\"" : s;
    }

    private String tipoEntrega(String entorno) {
        if (entorno == null) return "Nacional";
        return switch (entorno) {
            case "local"  -> "Local MTY";
            case "manual" -> "Manual";
            case "test"   -> "Nacional (TEST)";
            default       -> "Nacional";
        };
    }
}
