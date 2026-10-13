package br.com.restaurante.infrastructure.web.controllers;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthWireMockIntegrationTest {

    private static WireMockServer wireMockServer;
    private HttpClient httpClient;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        httpClient = HttpClient.newHttpClient();
    }

    @Test
    void wireMock_verifyExternalTokenValidation_success() throws IOException, InterruptedException {
        // Mock external identity provider verifying token
        stubFor(get(urlEqualTo("/oauth/token/verify"))
                .withHeader("Authorization", equalTo("Bearer valid-sample-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "active": true,
                                  "sub": "1",
                                  "email": "dono@restaurante.com",
                                  "userType": "DONO_DE_RESTAURANTE"
                                }
                                """)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + wireMockServer.port() + "/oauth/token/verify"))
                .header("Authorization", "Bearer valid-sample-token")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("DONO_DE_RESTAURANTE"));
        verify(getRequestedFor(urlEqualTo("/oauth/token/verify"))
                .withHeader("Authorization", equalTo("Bearer valid-sample-token")));
    }

    @Test
    void wireMock_verifyExternalTokenValidation_unauthorized() throws IOException, InterruptedException {
        stubFor(get(urlEqualTo("/oauth/token/verify"))
                .withHeader("Authorization", equalTo("Bearer invalid-token"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "error": "invalid_token",
                                  "error_description": "The token is expired or invalid"
                                }
                                """)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + wireMockServer.port() + "/oauth/token/verify"))
                .header("Authorization", "Bearer invalid-token")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("invalid_token"));
    }
}
