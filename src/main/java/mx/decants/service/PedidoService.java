package mx.decants.service;

import mx.decants.dto.PedidoDTO;
import mx.decants.entity.EstadoPedido;
import mx.decants.entity.Pedido;
import mx.decants.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Pedido crearPedido(PedidoDTO dto) {
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
        pedido.setEstadoPedido(EstadoPedido.NUEVO);
        return pedidoRepository.save(pedido);
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
