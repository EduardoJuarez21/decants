package mx.decants.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comision_pagos", uniqueConstraints = @UniqueConstraint(columnNames = {"vendedor", "mes"}))
public class ComisionPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String vendedor;

    @Column(nullable = false, length = 7)
    private String mes; // formato "yyyy-MM"

    @Column(nullable = false)
    private Integer montoPagado;

    private String notas;

    @Column(nullable = false)
    private LocalDateTime fechaPago;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }

    public String getMes() { return mes; }
    public void setMes(String mes) { this.mes = mes; }

    public Integer getMontoPagado() { return montoPagado; }
    public void setMontoPagado(Integer montoPagado) { this.montoPagado = montoPagado; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
}
