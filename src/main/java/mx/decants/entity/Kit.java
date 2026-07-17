package mx.decants.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "kits")
public class Kit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String nombre;

    private Integer precio; // null = "Consultar precio"

    private boolean precioDesde; // true = "Desde $X", false = "$X"

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private Integer orden;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getPrecio() { return precio; }
    public void setPrecio(Integer precio) { this.precio = precio; }

    public boolean isPrecioDesde() { return precioDesde; }
    public void setPrecioDesde(boolean precioDesde) { this.precioDesde = precioDesde; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
