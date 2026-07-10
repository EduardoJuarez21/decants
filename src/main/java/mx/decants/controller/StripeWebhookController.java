package mx.decants.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import mx.decants.service.PedidoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stripe")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    private final PedidoService pedidoService;

    public StripeWebhookController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping(value = "/webhook", consumes = "application/json")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Webhook recibido pero STRIPE_WEBHOOK_SECRET no está configurado, ignorando");
            return ResponseEntity.ok("ignored");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Webhook con firma inválida rechazado: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        log.info("Webhook Stripe recibido: {}", event.getType());

        if ("checkout.session.completed".equals(event.getType())) {
            event.getDataObjectDeserializer().getObject().ifPresent(obj -> {
                Session session = (Session) obj;
                if ("paid".equals(session.getPaymentStatus())) {
                    try {
                        pedidoService.confirmarPorSession(session.getId());
                    } catch (Exception e) {
                        log.error("Error al confirmar pedido para session {}: {}", session.getId(), e.getMessage());
                    }
                }
            });
        }

        return ResponseEntity.ok("ok");
    }
}
