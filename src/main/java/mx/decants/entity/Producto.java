package mx.decants.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String categoria;        // "alta-perfumeria" | "nicho-arabe"

    @Column(nullable = false)
    private String genero;           // "hombre" | "mujer" | "unisex"

    @Column(nullable = false)
    private String familia;

    @Column(nullable = false)
    private String notas;

    private String caracteristicas;  // ej. "Dulce · Juvenil · Fresco" — cómo se percibe, no de qué está hecho

    private String inspiracion;      // ej. "Recuerda a: Creed Aventus" — comparación olfativa, texto libre

    @Column(nullable = false)
    private String imagenPrincipal;

    private String imagenCaracteristicas;

    @Column(nullable = false)
    private String claseCss;

    @Column(nullable = false)
    private Integer precio;          // precio 10 ml (o único)

    private Integer precio5ml;       // null si no tiene presentación 5 ml

    private Integer precio3ml;       // null si no tiene presentación 3 ml

    @Column(nullable = false)
    private Double calificacion;

    private boolean bestSeller;

    private boolean soloItem;        // true = solo disponible individual / paquete exclusivo

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private Integer orden;

    private Integer stock; // null = ilimitado, 0 = agotado

    @Column(nullable = false)
    private boolean promoActivo;

    private Integer descuentoPorcentaje; // 1-99, solo aplica si promoActivo = true

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getFamilia() { return familia; }
    public void setFamilia(String familia) { this.familia = familia; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public String getCaracteristicas() { return caracteristicas; }
    public void setCaracteristicas(String caracteristicas) { this.caracteristicas = caracteristicas; }

    public String getInspiracion() { return inspiracion; }
    public void setInspiracion(String inspiracion) { this.inspiracion = inspiracion; }

    public String getImagenPrincipal() { return imagenPrincipal; }
    public void setImagenPrincipal(String imagenPrincipal) { this.imagenPrincipal = imagenPrincipal; }

    public String getImagenCaracteristicas() { return imagenCaracteristicas; }
    public void setImagenCaracteristicas(String v) { this.imagenCaracteristicas = v; }

    public String getClaseCss() { return claseCss; }
    public void setClaseCss(String claseCss) { this.claseCss = claseCss; }

    public Integer getPrecio() { return precio; }
    public void setPrecio(Integer precio) { this.precio = precio; }

    public Integer getPrecio5ml() { return precio5ml; }
    public void setPrecio5ml(Integer precio5ml) { this.precio5ml = precio5ml; }

    public Integer getPrecio3ml() { return precio3ml; }
    public void setPrecio3ml(Integer precio3ml) { this.precio3ml = precio3ml; }

    public Double getCalificacion() { return calificacion; }
    public void setCalificacion(Double calificacion) { this.calificacion = calificacion; }

    public boolean isBestSeller() { return bestSeller; }
    public void setBestSeller(boolean bestSeller) { this.bestSeller = bestSeller; }

    public boolean isSoloItem() { return soloItem; }
    public void setSoloItem(boolean soloItem) { this.soloItem = soloItem; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public boolean isPromoActivo() { return promoActivo; }
    public void setPromoActivo(boolean promoActivo) { this.promoActivo = promoActivo; }

    public Integer getDescuentoPorcentaje() { return descuentoPorcentaje; }
    public void setDescuentoPorcentaje(Integer descuentoPorcentaje) { this.descuentoPorcentaje = descuentoPorcentaje; }

    // --- Precios con descuento (null si no hay promo activa) ---

    public Integer getPrecioConDescuento() { return conDescuento(precio); }
    public Integer getPrecio5mlConDescuento() { return conDescuento(precio5ml); }
    public Integer getPrecio3mlConDescuento() { return conDescuento(precio3ml); }

    private Integer conDescuento(Integer precioBase) {
        if (!promoActivo || descuentoPorcentaje == null || precioBase == null) return null;
        return precioBase - (precioBase * descuentoPorcentaje / 100);
    }
}
