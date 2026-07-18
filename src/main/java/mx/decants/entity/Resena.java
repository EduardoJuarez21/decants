package mx.decants.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resenas", indexes = {
    @Index(name = "idx_resenas_aprobada", columnList = "aprobada"),
    @Index(name = "idx_resenas_fecha",    columnList = "fecha")
})
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 1000)
    private String texto;

    @Column(nullable = false)
    private Integer calificacion; // 1-5

    @Column(nullable = false)
    private boolean aprobada = false;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    protected void onCreate() {
        if (fecha == null) fecha = LocalDateTime.now();
    }

    public Resena() {}

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public String getNombre()                  { return nombre; }
    public void setNombre(String n)            { this.nombre = n; }

    public String getTexto()                   { return texto; }
    public void setTexto(String t)             { this.texto = t; }

    public Integer getCalificacion()           { return calificacion; }
    public void setCalificacion(Integer c)     { this.calificacion = c; }

    public boolean isAprobada()                { return aprobada; }
    public void setAprobada(boolean a)         { this.aprobada = a; }

    public LocalDateTime getFecha()            { return fecha; }
    public void setFecha(LocalDateTime f)      { this.fecha = f; }
}
