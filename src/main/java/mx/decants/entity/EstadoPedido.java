package mx.decants.entity;

public enum EstadoPedido {

    NUEVO("Nuevo"),
    EN_REVISION("En revisión"),
    CONFIRMADO("Confirmado"),
    CANCELADO("Cancelado"),
    ENTREGADO("Entregado");

    private final String etiqueta;

    EstadoPedido(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
