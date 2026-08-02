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
import java.time.YearMonth;
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

    private static final Set<EstadoPedido> ESTADOS_VALIDOS = Set.of(
        EstadoPedido.CREADO, EstadoPedido.CONFIRMADO,
        EstadoPedido.LISTO_PARA_ENVIO, EstadoPedido.ENVIADO, EstadoPedido.ENTREGADO
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

    private int resolverPrecio(Producto p, String variant) {
        if ("botella".equals(variant)) {
            return p.getPrecioBotella() != null ? p.getPrecioBotella() : p.getPrecio();
        }
        if ("3ml".equals(variant) && p.getPrecio3ml() != null) {
            return p.getPrecio3mlConDescuento() != null ? p.getPrecio3mlConDescuento() : p.getPrecio3ml();
        }
        if ("5ml".equals(variant) && p.getPrecio5ml() != null) {
            return p.getPrecio5mlConDescuento() != null ? p.getPrecio5mlConDescuento() : p.getPrecio5ml();
        }
        return p.getPrecioConDescuento() != null ? p.getPrecioConDescuento() : p.getPrecio();
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
                    boolean esBotella = "botella".equals(variant);
                    Producto p = productoRepository.findById(productId).orElseThrow();
                    if (esBotella) {
                        Integer stockDisponible = p.getStockBotella();
                        if (stockDisponible != null && stockDisponible < qty) {
                            String msg = stockDisponible == 0
                                ? p.getNombre() + " está agotado"
                                : p.getNombre() + " solo tiene " + stockDisponible + " unidad(es) disponible(s)";
                            throw new IllegalArgumentException(msg);
                        }
                    } else {
                        int mlNecesario = qty * mlDeVariante(variant);
                        Integer mlDisponible = p.getStock();
                        if (mlDisponible != null && mlDisponible < mlNecesario) {
                            String msg = mlDisponible == 0
                                ? p.getNombre() + " está agotado"
                                : p.getNombre() + " solo tiene " + mlDisponible + " ml disponibles";
                            throw new IllegalArgumentException(msg);
                        }
                    }
                    int price = resolverPrecio(p, variant);
                    PedidoItem item = new PedidoItem();
                    item.setProducto(p);
                    item.setNombre(p.getNombre());
                    item.setVariante(esBotella ? "Frasco " + p.getMlBotella() + "ml" : variant);
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

                int price = resolverPrecio(p, variant);

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

    private static final int UMBRAL_ALERTA_BOTELLA = 2;
    private static final int UMBRAL_ALERTA_ML       = 10;

    private void descontarStock(List<PedidoItem> items) {
        if (items == null) return;
        for (PedidoItem item : items) {
            Producto p = item.getProducto();
            if (p == null) continue;
            boolean esBotella = item.getVariante() != null && item.getVariante().startsWith("Frasco ");
            if (esBotella) {
                if (p.getStockBotella() != null) {
                    p.setStockBotella(Math.max(0, p.getStockBotella() - item.getCantidad()));
                    productoRepository.save(p);
                    log.info("Stock frasco producto #{} ({}) → {}", p.getId(), p.getNombre(), p.getStockBotella());
                    if (p.getStockBotella() <= UMBRAL_ALERTA_BOTELLA) {
                        telegramService.notificarStockBajo(p.getNombre() + " (frasco)", p.getStockBotella(), "unidad(es)");
                    }
                }
            } else if (p.getStock() != null) {
                int mlVendido = item.getCantidad() * mlDeVariante(item.getVariante());
                p.setStock(Math.max(0, p.getStock() - mlVendido));
                productoRepository.save(p);
                log.info("Stock producto #{} ({}) → {} ml", p.getId(), p.getNombre(), p.getStock());
                if (p.getStock() <= UMBRAL_ALERTA_ML) {
                    telegramService.notificarStockBajo(p.getNombre(), p.getStock(), "ml");
                }
            }
        }
    }

    private static int mlDeVariante(String variante) {
        return switch (variante) {
            case "3ml"  -> 3;
            case "5ml"  -> 5;
            case "10ml" -> 10;
            default     -> 10;
        };
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

    @Transactional(readOnly = true)
    public int ventasVendedorMesActual(String vendedor) {
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return pedidoRepository
            .findByVendedorAndFechaCreacionAfterAndEstadoPedidoNot(vendedor, inicioMes, EstadoPedido.CANCELADO)
            .stream()
            .mapToInt(p -> p.getTotalPagado() != null ? p.getTotalPagado() : 0)
            .sum();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> comisionVendedor(String vendedor, YearMonth mes) {
        LocalDateTime desde = mes.atDay(1).atStartOfDay();
        LocalDateTime hasta = mes.plusMonths(1).atDay(1).atStartOfDay();
        List<Pedido> pedidos = pedidoRepository.findByVendedorAndFechaCreacionBetweenAndEstadoPedidoNot(
            vendedor, desde, hasta, EstadoPedido.CANCELADO);

        int ventasTotales = pedidos.stream()
            .mapToInt(p -> p.getTotalPagado() != null ? p.getTotalPagado() : 0)
            .sum();

        Map<String, Map<String, Object>> detallePorClave = new LinkedHashMap<>();
        double comisionTotal = 0;
        for (Pedido pedido : pedidos) {
            for (PedidoItem item : pedido.getItems()) {
                Producto p = item.getProducto();
                if (p == null) continue;
                Double comisionUnit = comisionUnitaria(p, item.getVariante());
                if (comisionUnit == null) continue;

                double subtotal = comisionUnit * item.getCantidad();
                comisionTotal += subtotal;

                String clave = item.getNombre() + "|" + item.getVariante();
                Map<String, Object> fila = detallePorClave.computeIfAbsent(clave, k -> {
                    Map<String, Object> f = new LinkedHashMap<>();
                    f.put("nombre", item.getNombre());
                    f.put("variante", item.getVariante());
                    f.put("comisionUnitaria", comisionUnit);
                    f.put("cantidad", 0);
                    f.put("subtotal", 0.0);
                    return f;
                });
                fila.put("cantidad", (int) fila.get("cantidad") + item.getCantidad());
                fila.put("subtotal", (double) fila.get("subtotal") + subtotal);
            }
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("ventasTotales", ventasTotales);
        resultado.put("comisionTotal", comisionTotal);
        resultado.put("detalle", new ArrayList<>(detallePorClave.values()));
        return resultado;
    }

    private static Double comisionUnitaria(Producto p, String variante) {
        return switch (variante) {
            case "3ml"  -> p.getComisionFamiliar3ml();
            case "5ml"  -> p.getComisionFamiliar5ml();
            case "10ml" -> p.getComisionFamiliar();
            default     -> null;
        };
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
                                     String itemsJson, Integer total,
                                     String direccion, String latitud, String longitud,
                                     String comentarios, String estadoStr, String vendedor) {
        Pedido pedido = new Pedido();
        List<PedidoItem> items = buildItemsManual(itemsJson);
        items.forEach(it -> it.setPedido(pedido));

        pedido.setNombreCliente(nombre.trim());
        pedido.setTelefono(telefono.trim());
        pedido.setEmail(email != null && !email.isBlank() ? email.trim() : null);
        pedido.setTipoProducto("Venta directa");
        pedido.setCantidad(items.stream().mapToInt(PedidoItem::getCantidad).sum());
        pedido.setItems(items);
        pedido.setProductosSeleccionados(buildResumen(items));
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
        pedido.setVendedor(vendedor != null && !vendedor.isBlank() ? vendedor.trim() : null);
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
        descontarStock(items);
        log.info("Pedido manual #{} creado — cliente: {}, total: ${} MXN", saved.getId(), nombre, total);
        telegramService.notificarNuevoPedido(saved);
        return saved;
    }

    private List<PedidoItem> buildItemsManual(String itemsJson) {
        List<PedidoItem> items = new ArrayList<>();
        try {
            JsonNode nodos = objectMapper.readTree(itemsJson);
            for (JsonNode nodo : nodos) {
                long productoId = nodo.get("productoId").asLong();
                String variante = nodo.get("variante").asText("10ml");
                int cantidad = nodo.get("cantidad").asInt(1);
                int precioUnitario = nodo.get("precioUnitario").asInt();

                Producto p = productoRepository.findById(productoId)
                        .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
                boolean esBotella = variante.startsWith("Frasco ");
                if (esBotella) {
                    Integer stockDisponible = p.getStockBotella();
                    if (stockDisponible != null && stockDisponible < cantidad) {
                        String msg = stockDisponible == 0
                            ? p.getNombre() + " está agotado"
                            : p.getNombre() + " solo tiene " + stockDisponible + " unidad(es) disponible(s)";
                        throw new IllegalArgumentException(msg);
                    }
                } else {
                    int mlNecesario = cantidad * mlDeVariante(variante);
                    Integer mlDisponible = p.getStock();
                    if (mlDisponible != null && mlDisponible < mlNecesario) {
                        String msg = mlDisponible == 0
                            ? p.getNombre() + " está agotado"
                            : p.getNombre() + " solo tiene " + mlDisponible + " ml disponibles";
                        throw new IllegalArgumentException(msg);
                    }
                }

                PedidoItem item = new PedidoItem();
                item.setProducto(p);
                item.setNombre(p.getNombre());
                item.setVariante(variante);
                item.setCantidad(cantidad);
                item.setPrecioUnitario(precioUnitario);
                items.add(item);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Productos inválidos");
        }
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Agrega al menos un producto.");
        }
        return items;
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
                boolean esLocal = "local".equals(p.getEntorno());
                if (esLocal && (nuevoEstado == EstadoPedido.LISTO_PARA_ENVIO || nuevoEstado == EstadoPedido.ENVIADO)) {
                    log.warn("Pedido #{} es local — estado {} no permitido", id, estadoStr);
                    return;
                }
                p.setEstadoPedido(nuevoEstado);
                pedidoRepository.save(p);
                log.info("Pedido #{} → estado: {}", id, estadoStr);
                switch (nuevoEstado) {
                    case CREADO           -> emailService.enviarCreado(p);
                    case CONFIRMADO       -> emailService.enviarConfirmadoPorAdmin(p);
                    case LISTO_PARA_ENVIO -> emailService.enviarListoParaEnvio(p);
                    case ENVIADO          -> emailService.enviarNotificacionEnvio(p);
                    case ENTREGADO        -> emailService.enviarEntregado(p);
                    case CANCELADO        -> emailService.enviarCancelado(p);
                    default               -> {}
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

        Set<EstadoPedido> validos = ESTADOS_VALIDOS;

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

    @Transactional(readOnly = true)
    public Map<Long, Long> mlVendidoPorProducto() {
        Map<Long, Long> map = new LinkedHashMap<>();
        for (Object[] row : pedidoItemRepository.sumarMlVendidoPorProducto(ESTADOS_VALIDOS)) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
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
