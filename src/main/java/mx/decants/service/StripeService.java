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

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public Session crearCheckoutSession(Pedido pedido) throws StripeException {
        Stripe.apiKey = secretKey;

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(baseUrl + "/pedido/confirmacion?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(baseUrl + "/pedido/nuevo")
            .setCustomerEmail(pedido.getEmail())
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
        Stripe.apiKey = secretKey;
        Session session = Session.retrieve(sessionId);
        return "paid".equals(session.getPaymentStatus());
    }
}
