package mx.decants.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendedor_cobros", uniqueConstraints = @UniqueConstraint(columnNames = {"vendedor", "mes"}))
public class VendedorCobro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String vendedor;

    @Column(nullable = false, length = 7)
    private String mes; // formato "yyyy-MM"

    @Column(nullable = false)
    private Integer montoCobrado;

    private String notas;

    @Column(nullable = false)
    private LocalDateTime fechaCobro;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }

    public String getMes() { return mes; }
    public void setMes(String mes) { this.mes = mes; }

    public Integer getMontoCobrado() { return montoCobrado; }
    public void setMontoCobrado(Integer montoCobrado) { this.montoCobrado = montoCobrado; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public LocalDateTime getFechaCobro() { return fechaCobro; }
    public void setFechaCobro(LocalDateTime fechaCobro) { this.fechaCobro = fechaCobro; }
}
