// package com.example.courseservice.Configuration;

// import org.springframework.cloud.client.loadbalancer.LoadBalanced;
// import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.client.RestTemplate;

// @Configuration
// @LoadBalancerClient(name = "course-service", configuration = LoadBalancerConfiguration.class)
// public class WebConfig {

//     @LoadBalanced
//     @Bean
//     public RestTemplate restTemplate() {
//         return new RestTemplate();
//     }
// }

package com.example.courseservice.Configuration;

// import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

@Configuration
// @LoadBalancerClient(name = "COURSE-SERVICE", configuration = LoadBalancerConfiguration.class)
public class WebConfig {

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
