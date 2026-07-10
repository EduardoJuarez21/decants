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
import mx.decants.repository.PedidoRepository;
import mx.decants.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

@Service
@Transactional
public class PedidoService {

    private static final Map<String, Integer> PACKAGE_PRICES = Map.of(
        "individual", 99,
        "discovery",  279,
        "coleccion",  449,
        "exclusivo",  399,
        "regalo",     299
    );

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final CuponService cuponService;
    private final ObjectMapper objectMapper;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProductoRepository productoRepository,
                         ClienteRepository clienteRepository,
                         CuponService cuponService,
                         ObjectMapper objectMapper) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.cuponService = cuponService;
        this.objectMapper = objectMapper;
    }

    public Pedido crearPedido(PedidoDTO dto) {
        int total = calcularTotal(dto.getCartItemsJson(), dto.getPackageType());

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

        int totalFinal = total;
        if (dto.getCodigoCupon() != null && !dto.getCodigoCupon().isBlank()) {
            cuponService.validar(dto.getCodigoCupon()).ifPresent(cupon -> {
                int descuento = Math.round(total * cupon.getDescuentoPorcentaje() / 100f);
                pedido.setCodigoCuponAplicado(cupon.getCodigo());
                pedido.setDescuentoAplicado(descuento);
            });
        }
        int descuento = pedido.getDescuentoAplicado() != null ? pedido.getDescuentoAplicado() : 0;
        pedido.setTotalPagado(total - descuento);
        pedido.setDireccion(dto.getDireccion());
        if (dto.getLatitud() != null && !dto.getLatitud().isBlank()) {
            try { pedido.setLatitud(Double.parseDouble(dto.getLatitud())); } catch (NumberFormatException ignored) {}
        }
        if (dto.getLongitud() != null && !dto.getLongitud().isBlank()) {
            try { pedido.setLongitud(Double.parseDouble(dto.getLongitud())); } catch (NumberFormatException ignored) {}
        }
        pedido.setEstadoPedido(EstadoPedido.PENDIENTE_PAGO);
        pedido.setCliente(encontrarOCrearCliente(dto));
        return pedidoRepository.save(pedido);
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

    private int calcularTotal(String cartItemsJson, String packageType) {
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

    public void actualizarStripeSession(Long pedidoId, String sessionId) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        pedido.setStripeSessionId(sessionId);
        pedidoRepository.save(pedido);
    }

    public void cancelarPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        pedido.setEstadoPedido(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    public Pedido confirmarPorSession(String sessionId) {
        Pedido pedido = pedidoRepository.findByStripeSessionId(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada: " + sessionId));
        if (pedido.getEstadoPedido() == EstadoPedido.PENDIENTE_PAGO) {
            pedido.setEstadoPedido(EstadoPedido.CONFIRMADO);
            pedidoRepository.save(pedido);
        }
        return pedido;
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
}
