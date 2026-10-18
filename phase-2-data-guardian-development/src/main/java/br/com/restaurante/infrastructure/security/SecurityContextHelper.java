package br.com.restaurante.infrastructure.security;

import br.com.restaurante.core.domain.AuthenticatedUser;
import br.com.restaurante.core.domain.exceptions.AccessDeniedDomainException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityContextHelper {

    private SecurityContextHelper() {}

    public static Optional<AuthenticatedUser> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public static AuthenticatedUser getRequiredAuthenticatedUser() {
        return getAuthenticatedUser()
                .orElseThrow(() -> new AccessDeniedDomainException("Usuário não autenticado"));
    }

    public static Long getRequiredUserId() {
        return getRequiredAuthenticatedUser().getId();
    }
}
