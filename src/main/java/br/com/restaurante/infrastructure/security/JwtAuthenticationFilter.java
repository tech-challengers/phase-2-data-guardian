package br.com.restaurante.infrastructure.security;

import br.com.restaurante.application.ports.out.TokenPort;
import br.com.restaurante.core.domain.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final TokenPort tokenPort;

    public JwtAuthenticationFilter(TokenPort tokenPort) {
        this.tokenPort = tokenPort;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/v1/auth/login") || path.equals("/auth/login");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                if (tokenPort.validateToken(token)) {
                    AuthenticatedUser user = tokenPort.extractTokenData(token);
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (user.getRole() != null && !user.getRole().isBlank()) {
                        String roleName = user.getRole().trim().toUpperCase();
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
                        authorities.add(new SimpleGrantedAuthority(roleName));
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT autenticado com sucesso para usuário: {}", user.getEmail());
                } else {
                    log.warn("Token JWT inválido para requisição: {}", request.getRequestURI());
                }
            } catch (Exception ex) {
                log.error("Falha ao processar token JWT para {}: {}", request.getRequestURI(), ex.getMessage(), ex);
            }
        } else {
            log.debug("Sem token Bearer na requisição: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
