package com.example.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r
    .path("/users/**", "/auth/**")
    .uri("lb://USER-SERVICE"))
            .route("course-service", r -> r.path("/courses/**").uri("lb://COURSE-SERVICE"))
            .route("payment-service", r -> r.path("/payment/**").uri("lb://PAYMENT-SERVICE"))
            .route("assessment-service", r -> r.path("/tests/**").uri("lb://ASSESSMENT-SERVICE"))
            .build();
    }
}
// package com.example.apigateway;
// package com.example.apigateway;

// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.cloud.gateway.route.RouteLocator;
// import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
// import org.springframework.context.annotation.Bean;
// import java.io.IOException;
// import java.net.ConnectException;
// import java.time.Duration;

// @SpringBootApplication
// public class ApiGatewayApplication {
//     public static void main(String[] args) {
//         SpringApplication.run(ApiGatewayApplication.class, args);
//     }

//     @Bean
//     public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//         return builder.routes()
//             .route("user_service", r -> r.path("/users/**")
//                     .filters(f -> f.circuitBreaker(config -> config
//                             .setName("userServiceCB")
//                             .setFallbackUri("forward:/fallback/user"))
//                             .retry(r1 -> r1.setMaxAttempts(3)
//                                         .setWaitDuration(Duration.ofMillis(1000))
//                                         .setRetryExceptions(IOException.class, ConnectException.class))
//                             .requestTimeout(config -> config.setTimeout(Duration.ofMillis(5000))))
//                     .uri("lb://user-service"))
//             .route("course_service", r -> r.path("/courses/**")
//                     .filters(f -> f.circuitBreaker(config -> config
//                             .setName("courseServiceCB")
//                             .setFallbackUri("forward:/fallback/course"))
//                             .retry(r1 -> r1.setMaxAttempts(3)
//                                         .setWaitDuration(Duration.ofMillis(1000))
//                                         .setRetryExceptions(IOException.class, ConnectException.class))
//                             .requestTimeout(config -> config.setTimeout(Duration.ofMillis(5000))))
//                     .uri("lb://course-service"))
//             .route("payment_service", r -> r.path("/payments/**")
//                     .filters(f -> f.circuitBreaker(config -> config
//                             .setName("paymentServiceCB")
//                             .setFallbackUri("forward:/fallback/payment"))
//                             .retry(r1 -> r1.setMaxAttempts(3)
//                                         .setWaitDuration(Duration.ofMillis(1000))
//                                         .setRetryExceptions(IOException.class, ConnectException.class))
//                             .requestTimeout(config -> config.setTimeout(Duration.ofMillis(5000))))
//                     .uri("lb://payment-service"))
//             .route("assessment_service", r -> r.path("/assessments/**")
//                     .filters(f -> f.circuitBreaker(config -> config
//                             .setName("assessmentServiceCB")
//                             .setFallbackUri("forward:/fallback/assessment"))
//                             .retry(r1 -> r1.setMaxAttempts(3)
//                                         .setWaitDuration(Duration.ofMillis(1000))
//                                         .setRetryExceptions(IOException.class, ConnectException.class))
//                             .requestTimeout(config -> config.setTimeout(Duration.ofMillis(5000))))
//                     .uri("lb://assessment-service"))
//             .build();
//     }
// }
