package mx.decants.entity;

import jakarta.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @Column(length = 500)
    private String observaciones; // nombre real del cliente cuando el pedido queda a nombre de la vendedora, u otras notas internas

    @Column(columnDefinition = "TEXT")
    private String productosSeleccionados;

    @OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private String stripeSessionId;

    private Integer totalPagado;

    private Integer montoAbonado; // cuanto ha pagado el cliente hasta ahora; null = no se esta trackeando un anticipo

    private String codigoCuponAplicado;

    private Integer descuentoAplicado;

    @Column(length = 500)
    private String direccion;

    private String entorno;

    private String vendedor; // ej. "doris" — null si fue venta directa del sitio, sin vendedor asignado

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

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

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

    public Integer getMontoAbonado() { return montoAbonado; }
    public void setMontoAbonado(Integer montoAbonado) { this.montoAbonado = montoAbonado; }

    public Integer getSaldoPendiente() {
        return (montoAbonado != null && totalPagado != null) ? Math.max(0, totalPagado - montoAbonado) : null;
    }

    public String getCodigoCuponAplicado() { return codigoCuponAplicado; }
    public void setCodigoCuponAplicado(String codigoCuponAplicado) { this.codigoCuponAplicado = codigoCuponAplicado; }

    public Integer getDescuentoAplicado() { return descuentoAplicado; }
    public void setDescuentoAplicado(Integer descuentoAplicado) { this.descuentoAplicado = descuentoAplicado; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEntorno() { return entorno; }
    public void setEntorno(String entorno) { this.entorno = entorno; }

    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }

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

    // --- WhatsApp: mensaje y link pre-armados según el estado actual ---

    public String getWaMensaje() {
        String codigo = codigoPublico != null ? codigoPublico : ("#" + id);
        String cliente = nombreCliente != null ? nombreCliente : "";
        return switch (estadoPedido) {
            case PENDIENTE_PAGO -> "Hola " + cliente + "! Tu pedido " + codigo + " está pendiente de pago."
                + (getSaldoPendiente() != null
                    ? " Llevas $" + montoAbonado + " abonados, falta $" + getSaldoPendiente() + " MXN por liquidar."
                    : "")
                + " — Aura Decants MX";
            case CREADO -> "Hola " + cliente + "! Recibimos tu pedido " + codigo
                + ". Estamos revisando los detalles y pronto nos ponemos en contacto contigo. — Aura Decants MX";
            case CONFIRMADO -> "Hola " + cliente + "! Tu pedido " + codigo
                + " fue confirmado. Ya estamos preparando tus fragancias. — Aura Decants MX";
            case LISTO_PARA_ENVIO -> "Hola " + cliente + "! Tu pedido " + codigo
                + " está listo para enviarse. En breve recibirás el número de guía. — Aura Decants MX";
            case ENVIADO -> "Hola " + cliente + "! Tu pedido " + codigo + " está en camino."
                + (numeroGuia != null && !numeroGuia.isBlank() ? " Número de guía: " + numeroGuia + "." : "")
                + " — Aura Decants MX";
            case ENTREGADO -> "Hola " + cliente + "! Tu pedido " + codigo
                + " fue entregado. ¡Gracias por confiar en nosotros! — Aura Decants MX";
            case CANCELADO -> "Hola " + cliente + ", lamentamos informarte que tu pedido " + codigo
                + " fue cancelado. Si tienes dudas, contáctanos. — Aura Decants MX";
            default -> "Hola " + cliente + "! Tenemos una actualización sobre tu pedido " + codigo + ". — Aura Decants MX";
        };
    }

    public String getWaLink() {
        String tel = telefono != null ? telefono.replaceAll("[^0-9]", "") : "";
        if (tel.length() == 10) tel = "52" + tel;
        return "https://wa.me/" + tel + "?text=" + URLEncoder.encode(getWaMensaje(), StandardCharsets.UTF_8);
    }
}
