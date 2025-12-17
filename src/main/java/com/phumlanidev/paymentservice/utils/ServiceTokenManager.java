package com.phumlanidev.paymentservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
public class ServiceTokenManager {

  @Value("${keycloak.token-uri}")
  private String tokenUri;

  @Value("${keycloak.resource}")
  private String resource;

  @Value("${keycloak.credentials-secret}")
  private String secret;

  private String cachedToken;
  private Instant tokenExpiry = Instant.now();

  public String getAccessToken() {
    if (cachedToken != null && Instant.now().isBefore(tokenExpiry.minusSeconds(60))) {
      return cachedToken;
    }

    try {
      var headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      var body = new LinkedMultiValueMap<String, String>();
      body.add("grant_type", "client_credentials");
      body.add("client_id", resource);
      body.add("client_secret", secret);

      var entity = new HttpEntity<>(body, headers);

      RestTemplate restTemplate = new RestTemplate();
      var response = restTemplate.exchange(tokenUri, HttpMethod.POST, entity, Map.class);

      Map<String, Object> responseBody = response.getBody();
      assert responseBody != null;

      cachedToken = (String) responseBody.get("access_token");
      int expiresIn = (Integer) responseBody.get("expires_in");
      tokenExpiry = Instant.now().plusSeconds(expiresIn);

      log.info("Obtained new access token, expires in {} seconds", expiresIn);
      return cachedToken;
    } catch (Exception e) {
      log.error("Error obtaining access token: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

}
