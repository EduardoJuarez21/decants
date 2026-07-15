package mx.decants.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cupones")
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    private String descripcion;

    @Column(nullable = false)
    private Integer descuentoPorcentaje;

    private Integer montoMinimo;     // null = sin compra mínima

    private Integer descuentoMaximo; // null = sin tope de descuento

    private boolean activo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getDescuentoPorcentaje() { return descuentoPorcentaje; }
    public void setDescuentoPorcentaje(Integer descuentoPorcentaje) { this.descuentoPorcentaje = descuentoPorcentaje; }

    public Integer getMontoMinimo() { return montoMinimo; }
    public void setMontoMinimo(Integer montoMinimo) { this.montoMinimo = montoMinimo; }

    public Integer getDescuentoMaximo() { return descuentoMaximo; }
    public void setDescuentoMaximo(Integer descuentoMaximo) { this.descuentoMaximo = descuentoMaximo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
