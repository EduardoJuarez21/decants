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

    private String concentracion;    // "EDT" | "EDP" | "Parfum"

    private String caracteristicas;  // ej. "Dulce · Juvenil · Fresco" — cómo se percibe, no de qué está hecho

    private String inspiracion;      // ej. "Recuerda a: Creed Aventus" — comparación olfativa, texto libre

    @Column(nullable = false)
    private String imagenPrincipal;

    private String imagenCaracteristicas;

    @Column(nullable = false)
    private String claseCss;

    @Column(unique = true)
    private String slug;             // URL publica /perfume/{slug} -- se genera una vez y no cambia

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

    private String proveedor;

    private Double costoPorMl;

    private Double markup;   // multiplicador sobre el costo, ej. 1.8 = precio sugerido 80% arriba del costo

    private Integer precioBotella;   // precio de venta del frasco completo, null = no se vende así
    private Integer mlBotella;       // tamaño del frasco completo, ej. 100
    private Integer stockBotella;    // inventario de frascos, independiente de `stock` (decants). null = ilimitado, 0 = agotado

    private Double comisionFamiliar;     // comisión del vendedor familiar por venta de 10ml
    private Double comisionFamiliar5ml;
    private Double comisionFamiliar3ml;

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

    public String getConcentracion() { return concentracion; }
    public void setConcentracion(String concentracion) { this.concentracion = concentracion; }

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

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

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

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public Double getCostoPorMl() { return costoPorMl; }
    public void setCostoPorMl(Double costoPorMl) { this.costoPorMl = costoPorMl; }

    public Double getMarkup() { return markup; }
    public void setMarkup(Double markup) { this.markup = markup; }

    public Integer getPrecioBotella() { return precioBotella; }
    public void setPrecioBotella(Integer precioBotella) { this.precioBotella = precioBotella; }

    public Integer getMlBotella() { return mlBotella; }
    public void setMlBotella(Integer mlBotella) { this.mlBotella = mlBotella; }

    public Integer getStockBotella() { return stockBotella; }
    public void setStockBotella(Integer stockBotella) { this.stockBotella = stockBotella; }

    public Double getComisionFamiliar() { return comisionFamiliar; }
    public void setComisionFamiliar(Double comisionFamiliar) { this.comisionFamiliar = comisionFamiliar; }

    public Double getComisionFamiliar5ml() { return comisionFamiliar5ml; }
    public void setComisionFamiliar5ml(Double comisionFamiliar5ml) { this.comisionFamiliar5ml = comisionFamiliar5ml; }

    public Double getComisionFamiliar3ml() { return comisionFamiliar3ml; }
    public void setComisionFamiliar3ml(Double comisionFamiliar3ml) { this.comisionFamiliar3ml = comisionFamiliar3ml; }

    // --- Precios con descuento (null si no hay promo activa) ---

    public Integer getPrecioConDescuento() { return conDescuento(precio); }
    public Integer getPrecio5mlConDescuento() { return conDescuento(precio5ml); }
    public Integer getPrecio3mlConDescuento() { return conDescuento(precio3ml); }

    private Integer conDescuento(Integer precioBase) {
        if (!promoActivo || descuentoPorcentaje == null || precioBase == null) return null;
        return precioBase - (precioBase * descuentoPorcentaje / 100);
    }

    // --- Costo de producción, margen y precio sugerido (null si falta costoPorMl) ---

    public Double getCostoProduccion10ml() { return costoProduccion(10); }
    public Double getCostoProduccion5ml()  { return costoProduccion(5); }
    public Double getCostoProduccion3ml()  { return costoProduccion(3); }

    private Double costoProduccion(int ml) {
        return costoPorMl != null ? costoPorMl * ml : null;
    }

    public Double getMargenPorcentaje10ml() { return margenPorcentaje(precio, getCostoProduccion10ml()); }
    public Double getMargenPorcentaje5ml()  { return margenPorcentaje(precio5ml, getCostoProduccion5ml()); }
    public Double getMargenPorcentaje3ml()  { return margenPorcentaje(precio3ml, getCostoProduccion3ml()); }

    private static Double margenPorcentaje(Integer precioVenta, Double costo) {
        if (precioVenta == null || precioVenta == 0 || costo == null) return null;
        return ((precioVenta - costo) / precioVenta) * 100;
    }

    public Double getPrecioSugerido10ml() { return precioSugerido(getCostoProduccion10ml()); }
    public Double getPrecioSugerido5ml()  { return precioSugerido(getCostoProduccion5ml()); }
    public Double getPrecioSugerido3ml()  { return precioSugerido(getCostoProduccion3ml()); }

    private Double precioSugerido(Double costo) {
        return (costo != null && markup != null) ? costo * markup : null;
    }

    // --- Costo, margen y precio sugerido del frasco completo (null si falta costoPorMl o mlBotella) ---

    public Double getCostoBotella() {
        return (costoPorMl != null && mlBotella != null) ? costoPorMl * mlBotella : null;
    }

    public Double getMargenBotellaPorcentaje() { return margenPorcentaje(precioBotella, getCostoBotella()); }

    public Double getPrecioSugeridoBotella() { return precioSugerido(getCostoBotella()); }
}
