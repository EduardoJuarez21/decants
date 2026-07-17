package mx.decants.entity;

import jakarta.persistence.*;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    private static final String CODIGO_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 15)
    private String codigoPublico;

    @Column(nullable = false)
    private String nombreCliente;

    @Column(nullable = false)
    private String telefono;

    private String email;

    @Column(nullable = false)
    private String tipoProducto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(length = 100)
    private String textoPersonalizado;

    private String colorPreferido;

    private LocalDate fechaEvento;

    @Column(length = 500)
    private String comentarios;

    @Column(columnDefinition = "TEXT")
    private String productosSeleccionados;

    @OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private String stripeSessionId;

    private Integer totalPagado;

    private String codigoCuponAplicado;

    private Integer descuentoAplicado;

    @Column(length = 500)
    private String direccion;

    private String entorno;

    private Double latitud;

    private Double longitud;

    @Column(length = 100)
    private String numeroGuia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estadoPedido = EstadoPedido.NUEVO;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (codigoPublico == null) codigoPublico = generarCodigo();
    }

    private static String generarCodigo() {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder("AURA-");
        for (int i = 0; i < 8; i++) sb.append(CODIGO_CHARS.charAt(rnd.nextInt(CODIGO_CHARS.length())));
        return sb.toString();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoPublico() { return codigoPublico; }
    public void setCodigoPublico(String codigoPublico) { this.codigoPublico = codigoPublico; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(String tipoProducto) { this.tipoProducto = tipoProducto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getTextoPersonalizado() { return textoPersonalizado; }
    public void setTextoPersonalizado(String textoPersonalizado) { this.textoPersonalizado = textoPersonalizado; }

    public String getColorPreferido() { return colorPreferido; }
    public void setColorPreferido(String colorPreferido) { this.colorPreferido = colorPreferido; }

    public LocalDate getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDate fechaEvento) { this.fechaEvento = fechaEvento; }

    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }

    public String getProductosSeleccionados() { return productosSeleccionados; }
    public void setProductosSeleccionados(String productosSeleccionados) { this.productosSeleccionados = productosSeleccionados; }

    public List<PedidoItem> getItems() { return items; }
    public void setItems(List<PedidoItem> items) { this.items = items; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getStripeSessionId() { return stripeSessionId; }
    public void setStripeSessionId(String stripeSessionId) { this.stripeSessionId = stripeSessionId; }

    public Integer getTotalPagado() { return totalPagado; }
    public void setTotalPagado(Integer totalPagado) { this.totalPagado = totalPagado; }

    public String getCodigoCuponAplicado() { return codigoCuponAplicado; }
    public void setCodigoCuponAplicado(String codigoCuponAplicado) { this.codigoCuponAplicado = codigoCuponAplicado; }

    public Integer getDescuentoAplicado() { return descuentoAplicado; }
    public void setDescuentoAplicado(Integer descuentoAplicado) { this.descuentoAplicado = descuentoAplicado; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEntorno() { return entorno; }
    public void setEntorno(String entorno) { this.entorno = entorno; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getNumeroGuia() { return numeroGuia; }
    public void setNumeroGuia(String numeroGuia) { this.numeroGuia = numeroGuia; }

    public EstadoPedido getEstadoPedido() { return estadoPedido; }
    public void setEstadoPedido(EstadoPedido estadoPedido) { this.estadoPedido = estadoPedido; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
