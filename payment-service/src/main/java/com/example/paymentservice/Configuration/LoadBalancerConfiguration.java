package com.example.paymentservice.Configuration;

import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadBalancerConfiguration {

    @Bean
    public ReactorServiceInstanceLoadBalancer customLoadBalancer(ServiceInstanceListSupplier supplier) {
        return new CustomLoadBalancer(supplier);
    }
}
