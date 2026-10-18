package br.com.restaurante.infrastructure.security;

import br.com.restaurante.application.ports.out.TokenPort;
import br.com.restaurante.core.domain.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private final TokenPort tokenPort;

    public JwtAuthenticationFilter(TokenPort tokenPort) {
        this.tokenPort = tokenPort;
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
                }
            } catch (Exception ignored) {
                // Ignore and proceed, SecurityContext remains unauthenticated
            }
        }

        filterChain.doFilter(request, response);
    }
}
