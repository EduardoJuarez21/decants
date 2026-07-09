package mx.decants.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class PedidoDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    private String nombreCliente;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    // Null cuando el usuario deja el campo vacío (StringTrimmerEditor convierte "" → null)
    @Email(message = "El correo no tiene un formato válido")
    private String email;

    @NotBlank(message = "El tipo de producto es obligatorio")
    private String tipoProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @Size(max = 100, message = "El texto personalizado no puede superar los 100 caracteres")
    private String textoPersonalizado;

    private String colorPreferido;

    @FutureOrPresent(message = "La fecha del evento no puede ser una fecha pasada")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaEvento;

    @Size(max = 500, message = "Los comentarios no pueden superar los 500 caracteres")
    private String comentarios;

    private String productosSeleccionados;

    private Integer totalMxn;

    // --- Getters & Setters ---

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(String tipoProducto) { this.tipoProducto = tipoProducto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getTextoPersonalizado() { return textoPersonalizado; }
    public void setTextoPersonalizado(String textoPersonalizado) { this.textoPersonalizado = textoPersonalizado; }

    public String getColorPreferido() { return colorPreferido; }
    public void setColorPreferido(String colorPreferido) { this.colorPreferido = colorPreferido; }

    public LocalDate getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDate fechaEvento) { this.fechaEvento = fechaEvento; }

    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }

    public String getProductosSeleccionados() { return productosSeleccionados; }
    public void setProductosSeleccionados(String v) { this.productosSeleccionados = v; }

    public Integer getTotalMxn() { return totalMxn; }
    public void setTotalMxn(Integer totalMxn) { this.totalMxn = totalMxn; }
}
