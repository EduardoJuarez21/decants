package mx.decants.config;

import jakarta.servlet.http.HttpServletRequest;
import mx.decants.repository.VendedorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    static final String ADMIN_BASE = "/aura-gestion";

    private final LoginAttemptService loginAttemptService;

    public SecurityConfig(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' https://maps.googleapis.com https://maps.gstatic.com https://static.cloudflareinsights.com; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                    "font-src 'self' https://fonts.gstatic.com; " +
                    "img-src 'self' data: https:; " +
                    "connect-src 'self' https://maps.googleapis.com https://maps.gstatic.com; " +
                    "frame-ancestors 'none'; " +
                    "object-src 'none'"
                ))
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(new AntPathRequestMatcher("/stripe/webhook"))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/stripe/webhook")).permitAll()
                .requestMatchers(new AntPathRequestMatcher(ADMIN_BASE + "/login")).permitAll()
                .requestMatchers(new AntPathRequestMatcher(ADMIN_BASE + "/logout")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/img/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher(ADMIN_BASE + "/comisiones")).hasAnyRole("ADMIN", "VENDEDOR")
                .requestMatchers(new AntPathRequestMatcher(ADMIN_BASE + "/**")).hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage(ADMIN_BASE + "/login")
                .loginProcessingUrl(ADMIN_BASE + "/login")
                .defaultSuccessUrl(ADMIN_BASE + "/pedidos", true)
                .failureUrl(ADMIN_BASE + "/login?error")
                .successHandler((req, res, auth) -> {
                    loginAttemptService.succeeded(getClientIp(req));
                    boolean esVendedor = auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"));
                    res.sendRedirect(esVendedor ? ADMIN_BASE + "/comisiones" : ADMIN_BASE + "/pedidos");
                })
                .failureHandler((req, res, ex) -> {
                    String ip = getClientIp(req);
                    loginAttemptService.failed(ip);
                    String redirect = loginAttemptService.isBlocked(ip)
                            ? ADMIN_BASE + "/login?bloqueado"
                            : ADMIN_BASE + "/login?error";
                    res.sendRedirect(redirect);
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl(ADMIN_BASE + "/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(
            @Value("${admin.usuario}") String adminUsuario,
            @Value("${admin.password}") String adminPassword,
            VendedorRepository vendedorRepository) {
        UserDetails admin = User.builder()
                .username(adminUsuario)
                .password(passwordEncoder().encode(adminPassword))
                .roles("ADMIN")
                .build();

        return username -> {
            if (admin.getUsername().equalsIgnoreCase(username)) {
                return admin;
            }
            return vendedorRepository.findByUsuarioIgnoreCase(username)
                    .filter(v -> v.isActivo())
                    .map(v -> new User(v.getUsuario(), v.getPasswordHash(),
                            List.of(new SimpleGrantedAuthority("ROLE_VENDEDOR"))))
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    static String getClientIp(HttpServletRequest request) {
        // El sitio esta detras de Cloudflare: CF-Connecting-IP es la IP real del visitante,
        // Cloudflare la sobreescribe siempre asi que el cliente no puede falsificarla.
        // X-Forwarded-For aqui terminaria en la IP del propio edge de Cloudflare (no la del
        // visitante), lo que agrupa por error a distintos visitantes bajo la misma IP.
        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isBlank()) {
            return cfConnectingIp.trim();
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] parts = forwarded.split(",");
            return parts[0].trim();
        }
        return request.getRemoteAddr();
    }
}
