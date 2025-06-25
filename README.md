[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-payment-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-payment-service)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-payment-service&metric=bugs)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-payment-service)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-payment-service&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-payment-service)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-payment-service&metric=coverage)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-payment-service)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-payment-service&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-payment-service)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-payment-service&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-payment-service)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-payment-service&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-payment-service)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-payment-service&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-payment-service)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-payment-service&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-payment-service)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-payment-service&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-payment-service)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-payment-service&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-payment-service)

# 💳 Payment Service

The **Payment Service** is responsible for handling customer payments in the eCommerce platform. It integrates with [Stripe](https://stripe.com) to initiate secure checkout sessions and process payment webhooks. After a successful payment, it updates the corresponding order and sends a confirmation email via the Notification Service.

---

## 📦 Features

- 🔐 Secured with Keycloak OAuth2 (Client Credentials flow)
- 💸 Create Stripe Checkout Sessions
- 📬 Listen to Stripe Webhooks (`checkout.session.completed`)
- ✅ Mark orders as PAID after payment
- 📧 Notify users via email using the Notification Service
- 🛡️ Inter-service communication secured with OAuth2

---

## ⚙️ Technologies Used

- Spring Boot 3
- Spring Security (OAuth2 Resource Server)
- Stripe Java SDK
- RestTemplate (secured with Bearer token)
- Redis (for caching, optional)
- Keycloak (as Identity Provider)

---

## 🚀 Endpoints

| Method | Endpoint                        | Description                              | Auth Required |
|--------|----------------------------------|------------------------------------------|---------------|
| POST   | `/api/v1/payments/checkout`     | Initiate Stripe Checkout Session         | ✅ Yes        |
| POST   | `/api/v1/stripe/webhook`        | Stripe Webhook Listener                  | ❌ No         |

---

## 🔐 Inter-Service Communication

| Consumer         | Target Service   | Endpoint                                    | Secured by Role           |
|------------------|------------------|---------------------------------------------|---------------------------|
| Payment Service  | Order Service     | `PUT /api/v1/order/mark-paid/{orderId}`     | `order-service-role`      |
| Payment Service  | Notification Service | `POST /api/v1/notifications/payment-confirmation` | `notification-service-role` |

---

## 🔧 Configuration

### `application.yml`

```yaml
server:
  port: 9600

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/ecommerce

keycloak:
  auth-server-url: http://localhost:8080
  realm: ecommerce
  resource: payment-service
  principle-attribute: preferred_username
  credentials:
    secret: your-client-secret
