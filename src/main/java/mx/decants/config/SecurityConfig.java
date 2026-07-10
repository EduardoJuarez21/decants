package mx.decants.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/stripe/webhook"))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/stripe/webhook")).permitAll()
                .requestMatchers(new AntPathRequestMatcher(ADMIN_BASE + "/login")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/img/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher(ADMIN_BASE + "/**")).authenticated()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage(ADMIN_BASE + "/login")
                .loginProcessingUrl(ADMIN_BASE + "/login")
                .defaultSuccessUrl(ADMIN_BASE + "/pedidos", true)
                .failureUrl(ADMIN_BASE + "/login?error")
                .successHandler((req, res, auth) -> {
                    loginAttemptService.succeeded(getClientIp(req));
                    res.sendRedirect(ADMIN_BASE + "/pedidos");
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
            @Value("${admin.usuario}") String usuario,
            @Value("${admin.password}") String password) {
        UserDetails admin = User.builder()
                .username(usuario)
                .password(passwordEncoder().encode(password))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    static String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
