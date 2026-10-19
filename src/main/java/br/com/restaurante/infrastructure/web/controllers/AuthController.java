package br.com.restaurante.infrastructure.web.controllers;

import br.com.restaurante.application.ports.in.AuthUseCase;
import br.com.restaurante.infrastructure.web.dto.LoginRequest;
import br.com.restaurante.infrastructure.web.dto.LoginResponse;
import br.com.restaurante.infrastructure.web.dto.UserResponse;
import br.com.restaurante.infrastructure.web.dto.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthUseCase authUseCase;

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
}
