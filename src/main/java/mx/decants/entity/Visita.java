package mx.decants.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visitas", indexes = {
    @Index(name = "idx_visitas_fecha",  columnList = "fecha"),
    @Index(name = "idx_visitas_fuente", columnList = "fuente")
})
public class Visita {

    public static final String TIPO_PAGINA            = "pagina";
    public static final String TIPO_DESCARGA_CATALOGO = "descarga_catalogo";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String fuente;

    @Column(length = 30)
    private String tipo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    protected void onCreate() {
        if (fecha == null) fecha = LocalDateTime.now();
    }

    public Visita() {}

    public Visita(String fuente) {
        this.fuente = fuente;
        this.tipo   = TIPO_PAGINA;
        this.fecha  = LocalDateTime.now();
    }

    public Visita(String fuente, String tipo) {
        this.fuente = fuente;
        this.tipo   = tipo;
        this.fecha  = LocalDateTime.now();
    }

    public Long getId()              { return id; }
    public String getFuente()        { return fuente; }
    public void setFuente(String f)  { this.fuente = f; }
    public String getTipo()          { return tipo; }
    public void setTipo(String t)    { this.tipo = t; }
    public LocalDateTime getFecha()  { return fecha; }
    public void setFecha(LocalDateTime f) { this.fecha = f; }
}
