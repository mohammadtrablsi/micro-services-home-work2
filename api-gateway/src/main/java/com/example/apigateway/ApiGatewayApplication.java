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

