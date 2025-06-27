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
            .route("user_service", r -> r.path("/users/**").uri("lb://user-service"))
            .route("course_service", r -> r.path("/courses/**").uri("lb://course-service"))
            .route("payment_service", r -> r.path("/payments/**").uri("lb://payment-service"))
            .route("assessment_service", r -> r.path("/assessments/**").uri("lb://assessment-service"))
            .build();
    }
}
