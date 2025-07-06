package com.example.userservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
// @LoadBalancerClient(name = "USER-SERVICE", configuration = LoadBalancerConfiguration.class)
public class RestTemplateConfig {
    @LoadBalanced 
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
