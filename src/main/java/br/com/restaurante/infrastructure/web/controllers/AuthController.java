package br.com.restaurante.infrastructure.web.controllers;

import br.com.restaurante.application.ports.in.AuthUseCase;
import br.com.restaurante.application.ports.in.TokenUseCase;
import br.com.restaurante.core.domain.AuthenticatedUser;
import br.com.restaurante.infrastructure.security.SecurityContextHelper;
import br.com.restaurante.infrastructure.web.dto.LoginRequest;
import br.com.restaurante.infrastructure.web.dto.LoginResponse;
import br.com.restaurante.infrastructure.web.dto.TokenDataResponse;
import br.com.restaurante.infrastructure.web.dto.UserResponse;
import br.com.restaurante.infrastructure.web.dto.UserType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthApi {

    private final AuthUseCase authUseCase;
    private final TokenUseCase tokenUseCase;

    public AuthController(AuthUseCase authUseCase, TokenUseCase tokenUseCase) {
        this.authUseCase = authUseCase;
        this.tokenUseCase = tokenUseCase;
    }

    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        AuthUseCase.LoginResult result = authUseCase.login(loginRequest.getEmail(), loginRequest.getPassword());

        LoginResponse response = new LoginResponse();
        response.setAccessToken(result.accessToken());
        response.setTokenType(result.tokenType());
        response.setExpiresIn(result.expiresIn());

        UserResponse userResponse = new UserResponse();
        userResponse.setId(result.user().getId());
        userResponse.setName(result.user().getName());
        userResponse.setEmail(result.user().getEmail());
        if (result.user().getUserType() != null) {
            UserType ut = new UserType();
            ut.setId(result.user().getUserType().getId());
            ut.setName(result.user().getUserType().getName());
            userResponse.setUserType(ut);
        }
        response.setUser(userResponse);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TokenDataResponse> getAuthenticatedUser() {
        AuthenticatedUser user = SecurityContextHelper.getRequiredAuthenticatedUser();
        TokenDataResponse response = new TokenDataResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setRole(user.getRole());
        return ResponseEntity.ok(response);
    }
}
