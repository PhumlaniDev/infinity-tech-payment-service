package com.phumlanidev.paymentservice.config;

import com.phumlanidev.paymentservice.utils.ServiceTokenManager;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
public class AuthFeignConfig {

  private final ServiceTokenManager serviceTokenManager;

  @Bean
  public RequestInterceptor requestInterceptor() {
    return requestTemplate -> {
      String serviceToken = serviceTokenManager.getAccessToken();
      requestTemplate.header("Authorization", "Bearer " + serviceToken);
//      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//      if (authentication != null && authentication.getCredentials() instanceof Jwt jwt) {
//        requestTemplate.header("Authorization", "Bearer " + jwt.getTokenValue());
//      }
    };
  }
}
