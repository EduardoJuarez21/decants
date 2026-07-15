package mx.decants.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // POST /pedido/crear: máx 5 pedidos por IP cada 10 minutos
    private static final int  PEDIDO_MAX       = 5;
    private static final long PEDIDO_WINDOW_MS = 10 * 60 * 1000L;

    // GET /api/cupones/validar: máx 30 peticiones por IP cada 5 minutos
    private static final int  CUPON_MAX        = 30;
    private static final long CUPON_WINDOW_MS  = 5 * 60 * 1000L;

    private final ConcurrentHashMap<String, Bucket> pedidoBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> cuponBuckets  = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String path   = req.getRequestURI();
        String method = req.getMethod();
        String ip     = getClientIp(req);

        boolean limited = false;

        if ("POST".equalsIgnoreCase(method) && "/pedido/crear".equals(path)) {
            limited = isLimited(ip, pedidoBuckets, PEDIDO_MAX, PEDIDO_WINDOW_MS);
        } else if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/cupones/validar")) {
            limited = isLimited(ip, cuponBuckets, CUPON_MAX, CUPON_WINDOW_MS);
        }

        if (limited) {
            res.setStatus(429);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"error\":\"Demasiadas solicitudes. Intenta m\\u00e1s tarde.\"}");
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean isLimited(String ip, ConcurrentHashMap<String, Bucket> buckets, int max, long windowMs) {
        return buckets.computeIfAbsent(ip, k -> new Bucket()).isLimited(max, windowMs);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class Bucket {
        private long windowStart = System.currentTimeMillis();
        private int count = 0;

        synchronized boolean isLimited(int max, long windowMs) {
            long now = System.currentTimeMillis();
            if (now - windowStart > windowMs) {
                windowStart = now;
                count = 0;
            }
            count++;
            return count > max;
        }
    }
}