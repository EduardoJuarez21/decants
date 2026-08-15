package mx.decants.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import mx.decants.entity.Pedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    @Value("${stripe.secret-key-live}")
    private String secretKeyLive;

    @Value("${stripe.secret-key-test}")
    private String secretKeyTest;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final ConfiguracionService configuracionService;

    public StripeService(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    private String resolverKey() {
        return "test".equals(configuracionService.getStripeModo()) ? secretKeyTest : secretKeyLive;
    }

    public Session crearCheckoutSession(Pedido pedido) throws StripeException {
        Stripe.apiKey = resolverKey();

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(baseUrl + "/pedido/confirmacion?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(baseUrl + "/pedido/nuevo")
            .setCustomerEmail(pedido.getEmail())
            // Meses sin intereses: solo aplica si la cuenta de Stripe lo tiene
            // habilitado (Configuracion > Metodos de pago), la tarjeta es
            // mexicana y el pedido es >= $300 MXN. Si no se cumple algo de
            // eso, Stripe simplemente no ofrece la opcion, sin romper el pago.
            .setPaymentMethodOptions(
                SessionCreateParams.PaymentMethodOptions.builder()
                    .setCard(
                        SessionCreateParams.PaymentMethodOptions.Card.builder()
                            .setInstallments(
                                SessionCreateParams.PaymentMethodOptions.Card.Installments.builder()
                                    .setEnabled(true)
                                    .build())
                            .build())
                    .build())
            .addLineItem(SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("mxn")
                    .setUnitAmount((long) pedido.getTotalPagado() * 100)
                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Decants MX — " + pedido.getTipoProducto())
                        .setDescription(pedido.getProductosSeleccionados())
                        .build())
                    .build())
                .build())
            .putMetadata("pedido_id", pedido.getId().toString())
            .build();

        return Session.create(params);
    }

    public boolean verificarPago(String sessionId) throws StripeException {
        Stripe.apiKey = resolverKey();
        Session session = Session.retrieve(sessionId);
        return "paid".equals(session.getPaymentStatus());
    }
}