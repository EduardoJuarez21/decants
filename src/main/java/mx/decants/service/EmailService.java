package mx.decants.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.decants.entity.Pedido;
import mx.decants.entity.PedidoItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final String RESEND_API = "https://api.resend.com/emails";

    @Value("${app.base-url:https://auradecants.mx}")
    private String baseUrl;

    private final ConfiguracionService configuracionService;
    private final ObjectMapper objectMapper;

    public EmailService(ConfiguracionService configuracionService, ObjectMapper objectMapper) {
        this.configuracionService = configuracionService;
        this.objectMapper = objectMapper;
    }

    public void enviarConfirmacion(Pedido pedido) {
        String apiKey = configuracionService.get("email_password", "");
        if (apiKey.isBlank()) return;
        if (pedido.getEmail() == null || pedido.getEmail().isBlank()) return;

        String from    = from();
        String waNum   = configuracionService.getWhatsappNegocio();
        String ref     = pedido.getCodigoPublico() != null ? pedido.getCodigoPublico() : ("#" + pedido.getId());
        String subject = "Tu pedido " + ref + " fue confirmado — Aura Decants MX";

        enviarAsync(apiKey, from, pedido.getEmail(), subject, buildHtml(pedido, waNum), pedido.getId(), "confirmación");
    }

    public void enviarNotificacionEnvio(Pedido pedido) {
        String apiKey = configuracionService.get("email_password", "");
        if (apiKey.isBlank()) return;
        if (pedido.getEmail() == null || pedido.getEmail().isBlank()) return;

        String from    = from();
        String waNum   = configuracionService.getWhatsappNegocio();
        String ref     = pedido.getCodigoPublico() != null ? pedido.getCodigoPublico() : ("#" + pedido.getId());
        String subject = "Tu pedido " + ref + " está en camino — Aura Decants MX";

        enviarAsync(apiKey, from, pedido.getEmail(), subject, buildEnvioHtml(pedido, waNum), pedido.getId(), "envío");
    }

    private String from() {
        String f = configuracionService.get("email_from", "");
        return f.isBlank() ? "Aura Decants MX <noreply@auradecantsmx.com>" : f;
    }

    private void enviarAsync(String apiKey, String from, String to,
                             String subject, String html, Long pedidoId, String tipo) {
        CompletableFuture.runAsync(() -> {
            try {
                String body = objectMapper.writeValueAsString(Map.of(
                    "from",    from,
                    "to",      List.of(to),
                    "subject", subject,
                    "html",    html
                ));
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

                HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                            log.info("Email de {} enviado → {} (pedido #{})", tipo, to, pedidoId);
                        } else {
                            log.warn("Resend respondió {} para email de {} (pedido #{}): {}",
                                     resp.statusCode(), tipo, pedidoId, resp.body());
                        }
                    })
                    .exceptionally(ex -> {
                        log.warn("Error enviando email de {} (pedido #{}): {}", tipo, pedidoId, ex.getMessage());
                        return null;
                    });
            } catch (Exception e) {
                log.warn("Error preparando email de {} (pedido #{}): {}", tipo, pedidoId, e.getMessage());
            }
        });
    }

    private String buildEnvioHtml(Pedido pedido, String waNum) {
        String codigo = pedido.getCodigoPublico() != null ? pedido.getCodigoPublico() : ("#" + pedido.getId());
        String trackingUrl = baseUrl + "/pedido/seguimiento?codigo=" + encode(pedido.getCodigoPublico() != null ? pedido.getCodigoPublico() : "");
        String waUrl = "https://wa.me/" + waNum + "?text="
                       + encode("Hola! Quiero consultar el envío de mi pedido " + codigo + ".");
        boolean tieneGuia = pedido.getNumeroGuia() != null && !pedido.getNumeroGuia().isBlank();

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>")
          .append("<meta name='viewport' content='width=device-width,initial-scale=1'>")
          .append("<title>Pedido enviado</title></head>")
          .append("<body style='margin:0;padding:0;background:#f5f5f5;font-family:Inter,Arial,sans-serif;'>")
          .append("<table width='100%' cellpadding='0' cellspacing='0' style='background:#f5f5f5;padding:32px 16px;'>")
          .append("<tr><td align='center'>")
          .append("<table width='100%' style='max-width:560px;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.07);'>")
          .append("<tr><td style='background:#1a1a1a;padding:28px 32px;text-align:center;'>")
          .append("<p style='margin:0;color:#c9a96e;font-size:1.5rem;font-weight:700;letter-spacing:1px;'>Aura Decants MX</p>")
          .append("</td></tr>")
          .append("<tr><td style='padding:32px 32px 16px;text-align:center;'>")
          .append("<div style='font-size:2.5rem;margin-bottom:12px;'>&#128666;</div>")
          .append("<h1 style='margin:0;font-size:1.3rem;color:#1a1a1a;font-weight:700;'>¡Tu pedido está en camino!</h1>")
          .append("<p style='margin:8px 0 0;color:#777;font-size:0.88rem;'>")
          .append("Hola <strong>").append(esc(pedido.getNombreCliente())).append("</strong>, ")
          .append("tu pedido <strong>").append(esc(codigo)).append("</strong> ha sido enviado.")
          .append("</p></td></tr>")
          .append("<tr><td style='padding:0 32px 24px;'>")
          .append("<table width='100%' style='background:#fafaf8;border-radius:10px;border:1px solid #ececec;'>")
          .append("<tr><td style='padding:16px 20px;").append(tieneGuia ? "border-bottom:1px solid #ececec;" : "").append("'>")
          .append("<span style='font-size:0.7rem;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;color:#999;'>Pedido</span>")
          .append("<p style='margin:4px 0 0;font-size:1.1rem;font-weight:700;color:#c9a96e;'>").append(esc(codigo)).append("</p>")
          .append("</td></tr>");
        if (tieneGuia) {
            sb.append("<tr><td style='padding:16px 20px;'>")
              .append("<span style='font-size:0.7rem;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;color:#999;'>Número de guía</span>")
              .append("<p style='margin:4px 0 0;font-size:1rem;font-weight:700;color:#1a1a1a;font-family:monospace;'>")
              .append(esc(pedido.getNumeroGuia())).append("</p>")
              .append("<p style='margin:4px 0 0;font-size:0.78rem;color:#999;'>Puedes rastrear tu paquete con esta guía en el sitio de tu paquetería.</p>")
              .append("</td></tr>");
        }
        sb.append("</table></td></tr>")
          .append("<tr><td style='padding:8px 32px 16px;text-align:center;'>")
          .append("<a href='").append(trackingUrl).append("' ")
          .append("style='display:inline-block;padding:12px 28px;background:#1a1a1a;color:#fff;text-decoration:none;")
          .append("border-radius:8px;font-size:0.88rem;font-weight:600;'>")
          .append("&#128230; Seguir mi pedido</a></td></tr>")
          .append("<tr><td style='padding:0 32px 32px;text-align:center;'>")
          .append("<a href='").append(waUrl).append("' ")
          .append("style='display:inline-block;padding:11px 28px;background:#25d366;color:#fff;text-decoration:none;")
          .append("border-radius:8px;font-size:0.88rem;font-weight:600;'>")
          .append("&#128172; Contáctanos por WhatsApp</a></td></tr>")
          .append("<tr><td style='background:#f8f8f8;border-top:1px solid #ececec;padding:20px 32px;text-align:center;'>")
          .append("<p style='margin:0;font-size:0.75rem;color:#aaa;'>&copy; 2025 Aura Decants MX</p>")
          .append("</td></tr></table></td></tr></table></body></html>");
        return sb.toString();
    }

    private String buildHtml(Pedido pedido, String waNum) {
        boolean esLocal = "local".equalsIgnoreCase(pedido.getEntorno());
        String codigo = pedido.getCodigoPublico() != null ? pedido.getCodigoPublico() : ("#" + pedido.getId());
        String trackingUrl = baseUrl + "/pedido/seguimiento?codigo=" + encode(pedido.getCodigoPublico() != null ? pedido.getCodigoPublico() : "");
        String waUrl = "https://wa.me/" + waNum + "?text="
                       + encode("Hola! Quiero consultar sobre mi pedido " + codigo
                                + " a nombre de " + pedido.getNombreCliente() + ".");

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>")
          .append("<meta name='viewport' content='width=device-width,initial-scale=1'>")
          .append("<title>Pedido confirmado</title></head>")
          .append("<body style='margin:0;padding:0;background:#f5f5f5;font-family:Inter,Arial,sans-serif;'>")
          .append("<table width='100%' cellpadding='0' cellspacing='0' style='background:#f5f5f5;padding:32px 16px;'>")
          .append("<tr><td align='center'>")
          .append("<table width='100%' style='max-width:560px;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.07);'>")
          .append("<tr><td style='background:#1a1a1a;padding:28px 32px;text-align:center;'>")
          .append("<p style='margin:0;color:#c9a96e;font-size:1.5rem;font-weight:700;letter-spacing:1px;'>Aura Decants MX</p>")
          .append("<p style='margin:6px 0 0;color:rgba(255,255,255,0.6);font-size:0.8rem;letter-spacing:2px;text-transform:uppercase;'>")
          .append("Frascos personalizados premium</p>")
          .append("</td></tr>")
          .append("<tr><td style='padding:32px 32px 16px;text-align:center;'>")
          .append("<div style='width:56px;height:56px;background:#1a1a1a;border-radius:50%;display:inline-flex;align-items:center;justify-content:center;margin-bottom:16px;'>")
          .append("<span style='color:#c9a96e;font-size:1.6rem;'>&#10003;</span></div>")
          .append("<h1 style='margin:0;font-size:1.3rem;color:#1a1a1a;font-weight:700;'>")
          .append(esLocal ? "¡Pedido recibido!" : "¡Pago confirmado!")
          .append("</h1>")
          .append("<p style='margin:8px 0 0;color:#777;font-size:0.88rem;line-height:1.6;'>")
          .append(esLocal
              ? "Recibimos tu pedido. Nos contactamos por WhatsApp para coordinar la entrega. <strong>El pago es contra entrega.</strong>"
              : "Tu pago fue procesado con éxito. En breve te contactamos para coordinar el envío.")
          .append("</p></td></tr>")
          .append("<tr><td style='padding:0 32px 24px;'>")
          .append("<table width='100%' style='background:#fafaf8;border-radius:10px;border:1px solid #ececec;'>")
          .append("<tr><td style='padding:16px 20px;border-bottom:1px solid #ececec;'>")
          .append("<span style='font-size:0.7rem;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;color:#999;'>Código de pedido</span>")
          .append("<p style='margin:4px 0 0;font-size:1.2rem;font-weight:700;color:#c9a96e;'>").append(esc(codigo)).append("</p>")
          .append("</td></tr>")
          .append("<tr><td style='padding:16px 20px;border-bottom:1px solid #ececec;'>")
          .append("<span style='font-size:0.7rem;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;color:#999;'>Cliente</span>")
          .append("<p style='margin:4px 0 0;font-size:0.92rem;color:#1a1a1a;font-weight:600;'>").append(esc(pedido.getNombreCliente())).append("</p>")
          .append("</td></tr>");

        List<PedidoItem> items = pedido.getItems();
        if (items != null && !items.isEmpty()) {
            sb.append("<tr><td style='padding:16px 20px;border-bottom:1px solid #ececec;'>")
              .append("<span style='font-size:0.7rem;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;color:#999;'>Fragancias</span>");
            for (PedidoItem item : items) {
                sb.append("<div style='display:flex;justify-content:space-between;align-items:center;margin-top:8px;padding-left:10px;border-left:3px solid #c9a96e;'>")
                  .append("<span style='font-size:0.85rem;color:#1a1a1a;'>").append(esc(item.getNombre()));
                if (!"paquete".equals(item.getVariante())) {
                    sb.append(" <span style='color:#999;font-size:0.78rem;'>· ").append(esc(item.getVariante()));
                    if (item.getCantidad() > 1) sb.append(" x").append(item.getCantidad());
                    sb.append("</span>");
                }
                sb.append("</span>")
                  .append("<span style='font-size:0.8rem;color:#999;margin-left:12px;'>$").append(item.getSubtotal()).append("</span>")
                  .append("</div>");
            }
            sb.append("</td></tr>");
        } else if (pedido.getProductosSeleccionados() != null && !pedido.getProductosSeleccionados().isBlank()) {
            sb.append("<tr><td style='padding:16px 20px;border-bottom:1px solid #ececec;'>")
              .append("<span style='font-size:0.7rem;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;color:#999;'>Fragancias</span>")
              .append("<p style='margin:6px 0 0;font-size:0.85rem;color:#1a1a1a;'>").append(esc(pedido.getProductosSeleccionados())).append("</p>")
              .append("</td></tr>");
        }

        sb.append("<tr><td style='padding:16px 20px;'>")
          .append("<span style='font-size:0.7rem;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;color:#999;'>Total")
          .append(esLocal ? " (contra entrega)" : " pagado").append("</span>")
          .append("<p style='margin:4px 0 0;font-size:1.1rem;font-weight:700;color:#1a1a1a;'>$")
          .append(pedido.getTotalPagado()).append(" MXN</p>")
          .append("</td></tr>")
          .append("</table></td></tr>")
          .append("<tr><td style='padding:8px 32px 24px;text-align:center;'>")
          .append("<a href='").append(trackingUrl).append("' ")
          .append("style='display:inline-block;padding:12px 28px;background:#1a1a1a;color:#fff;text-decoration:none;")
          .append("border-radius:8px;font-size:0.88rem;font-weight:600;'>")
          .append("&#128230; Seguir mi pedido</a>")
          .append("</td></tr>")
          .append("<tr><td style='padding:0 32px 32px;text-align:center;'>")
          .append("<a href='").append(waUrl).append("' ")
          .append("style='display:inline-block;padding:11px 28px;background:#25d366;color:#fff;text-decoration:none;")
          .append("border-radius:8px;font-size:0.88rem;font-weight:600;'>")
          .append("&#128172; Contáctanos por WhatsApp</a>")
          .append("</td></tr>")
          .append("<tr><td style='background:#f8f8f8;border-top:1px solid #ececec;padding:20px 32px;text-align:center;'>")
          .append("<p style='margin:0;font-size:0.75rem;color:#aaa;'>")
          .append("&copy; 2025 Aura Decants MX &nbsp;·&nbsp; Frascos personalizados premium</p>")
          .append("</td></tr>")
          .append("</table></td></tr></table></body></html>");

        return sb.toString();
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String encode(String s) {
        if (s == null) return "";
        try { return java.net.URLEncoder.encode(s, "UTF-8"); } catch (Exception e) { return s; }
    }
}
