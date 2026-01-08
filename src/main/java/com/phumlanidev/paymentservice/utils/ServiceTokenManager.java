package com.phumlanidev.paymentservice.utils;

import com.phumlanidev.commonevents.events.auth.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceTokenManager {

    private final RestTemplate restTemplate;

    @Value("${keycloak.token-url}")
    private String tokenUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private String cachedToken;
    private Instant expiryTime;

    public synchronized String getAccessToken() {

        if (cachedToken != null && expiryTime != null && Instant.now().isBefore(expiryTime)) {
            return cachedToken;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> response =
                restTemplate.postForEntity(tokenUrl, request, TokenResponse.class);

        TokenResponse token = response.getBody();
        if (token == null) {
            throw new IllegalStateException("Token response was null");
        }

        cachedToken = token.getAccessToken();
        expiryTime = Instant.now().plusSeconds(token.getExpiresIn() - 30);

        log.info("🔐 Service token fetched successfully");
        return cachedToken;
    }
}
