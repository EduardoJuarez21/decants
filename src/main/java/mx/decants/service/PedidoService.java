package mx.decants.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.decants.dto.PedidoDTO;
import mx.decants.entity.EstadoPedido;
import mx.decants.entity.Pedido;
import mx.decants.entity.Producto;
import mx.decants.repository.PedidoRepository;
import mx.decants.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final ObjectMapper objectMapper;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProductoRepository productoRepository,
                         ObjectMapper objectMapper) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
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
        pedido.setProductosSeleccionados(dto.getProductosSeleccionados());
        pedido.setTotalPagado(total);
        pedido.setEstadoPedido(EstadoPedido.PENDIENTE_PAGO);
        return pedidoRepository.save(pedido);
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
}
