package mx.decants.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visitas", indexes = {
    @Index(name = "idx_visitas_fecha",  columnList = "fecha"),
    @Index(name = "idx_visitas_fuente", columnList = "fuente")
})
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String fuente;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    protected void onCreate() {
        if (fecha == null) fecha = LocalDateTime.now();
    }

    public Visita() {}

    public Visita(String fuente) {
        this.fuente = fuente;
        this.fecha  = LocalDateTime.now();
    }

    public Long getId()              { return id; }
    public String getFuente()        { return fuente; }
    public void setFuente(String f)  { this.fuente = f; }
    public LocalDateTime getFecha()  { return fecha; }
    public void setFecha(LocalDateTime f) { this.fecha = f; }
}
