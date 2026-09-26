package mx.decants.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendedores")
public class Vendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String usuario;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private int metaMonto = 1000;

    private String metaPremio;

    @Column(nullable = false)
    @ColumnDefault("15")
    private double comisionPorcentaje = 15;

    // Codigo del ultimo pedido cuya comision ya se le pago; pedidos con fecha
    // igual o posterior a este se muestran como pendientes en el historico de
    // comisiones. null = no se ha marcado ningun corte, todo se ve pendiente.
    private String pagadoHastaPedido;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getMetaMonto() { return metaMonto; }
    public void setMetaMonto(int metaMonto) { this.metaMonto = metaMonto; }

    public String getMetaPremio() { return metaPremio; }
    public void setMetaPremio(String metaPremio) { this.metaPremio = metaPremio; }

    public double getComisionPorcentaje() { return comisionPorcentaje; }
    public void setComisionPorcentaje(double comisionPorcentaje) { this.comisionPorcentaje = comisionPorcentaje; }

    public String getPagadoHastaPedido() { return pagadoHastaPedido; }
    public void setPagadoHastaPedido(String pagadoHastaPedido) { this.pagadoHastaPedido = pagadoHastaPedido; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
