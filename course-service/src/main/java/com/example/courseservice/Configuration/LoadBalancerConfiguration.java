// package com.example.courseservice.Configuration;

// import org.springframework.beans.factory.ObjectProvider;
// import org.springframework.cloud.client.ServiceInstance;
// import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
// import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
// import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
// import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplierBuilder;
// import org.springframework.context.ConfigurableApplicationContext;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// public class LoadBalancerConfiguration {

// @Bean
// public ServiceInstanceListSupplier serviceInstanceListSupplier(
//         ServiceInstanceListSupplierBuilder builder,
//         ConfigurableApplicationContext context) {
//     return builder
//             .withBlockingDiscoveryClient()
//             .build(context); // ✅ Pass context here
// }



//     @Bean
//     public ReactorLoadBalancer<ServiceInstance> customLoadBalancer(
//             ObjectProvider<ServiceInstanceListSupplier> provider) {
//         return new RoundRobinLoadBalancer(provider, "course-service");
//     }
// }
package com.example.courseservice.Configuration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadBalancerConfiguration {

    @Bean
    public ReactorLoadBalancer<ServiceInstance> roundRobinLoadBalancer(
            ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplier) {
        return new RoundRobinLoadBalancer(serviceInstanceListSupplier, "course-service");
    }
}
