package mx.decants.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.decants.entity.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private final ConfiguracionService configuracionService;
    private final ObjectMapper objectMapper;

    public TelegramService(ConfiguracionService configuracionService, ObjectMapper objectMapper) {
        this.configuracionService = configuracionService;
        this.objectMapper = objectMapper;
    }

    public void notificarNuevoPedido(Pedido pedido) {
        String token  = configuracionService.get("telegram_bot_token", "");
        String chatId = configuracionService.get("telegram_chat_id", "");
        if (token.isBlank() || chatId.isBlank()) return;

        String tipoEntrega = switch (pedido.getEntorno() != null ? pedido.getEntorno() : "") {
            case "local"   -> "🏠 Entrega local (contra entrega)";
            case "manual"  -> "✏️ Venta manual";
            case "test"    -> "🧪 Envío nacional (TEST)";
            default        -> "📦 Envío nacional";
        };

        String productos = pedido.getProductosSeleccionados() != null
            ? pedido.getProductosSeleccionados() : "—";

        String codigo = pedido.getCodigoPublico() != null ? pedido.getCodigoPublico() : ("—");
        String texto = String.format(
            "🛒 <b>Nuevo pedido #%d</b> · <code>%s</code>\n\n" +
            "👤 <b>%s</b>\n" +
            "📱 %s\n" +
            "📦 %s\n" +
            "💰 <b>$%d MXN</b>\n" +
            "%s",
            pedido.getId(),
            codigo,
            pedido.getNombreCliente(),
            pedido.getTelefono(),
            productos,
            pedido.getTotalPagado() != null ? pedido.getTotalPagado() : 0,
            tipoEntrega
        );

        enviar(token, chatId, texto);
    }

    public void notificarStockBajo(String nombreProducto, int stock) {
        String token  = configuracionService.get("telegram_bot_token", "");
        String chatId = configuracionService.get("telegram_chat_id", "");
        if (token.isBlank() || chatId.isBlank()) return;
        String texto = stock == 0
            ? "🚨 <b>Stock agotado</b>: " + nombreProducto
            : "⚠️ <b>Stock bajo</b>: " + nombreProducto + " — solo quedan <b>" + stock + "</b> unidad(es)";
        enviar(token, chatId, texto);
    }

    private void enviar(String token, String chatId, String texto) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                "chat_id",    chatId,
                "text",       texto,
                "parse_mode", "HTML"
            ));
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.telegram.org/bot" + token + "/sendMessage"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
            HTTP.sendAsync(req, HttpResponse.BodyHandlers.discarding())
                .exceptionally(ex -> {
                    log.warn("Error enviando notificación Telegram: {}", ex.getMessage());
                    return null;
                });
        } catch (Exception e) {
            log.warn("No se pudo preparar notificación Telegram: {}", e.getMessage());
        }
    }
}