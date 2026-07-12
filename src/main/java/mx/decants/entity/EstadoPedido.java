package mx.decants.entity;

public enum EstadoPedido {

    PENDIENTE_PAGO("Pendiente de pago"),
    CREADO("Creado"),
    CONFIRMADO("Confirmado"),
    LISTO_PARA_ENVIO("Listo para envío"),
    ENVIADO("Enviado"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado"),

    // Valores legacy — solo para compatibilidad con registros anteriores
    NUEVO("Nuevo"),
    EN_REVISION("En revisión");

    private final String etiqueta;

    EstadoPedido(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}