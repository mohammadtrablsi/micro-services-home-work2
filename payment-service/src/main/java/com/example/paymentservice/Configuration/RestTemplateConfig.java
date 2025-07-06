// // // package com.example.courseservice.Configuration;

// // import org.springframework.cloud.client.loadbalancer.LoadBalanced;
// // import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
// // import org.springframework.context.annotation.Bean;
// // import org.springframework.context.annotation.Configuration;
// // import org.springframework.web.client.RestTemplate;

// // @Configuration
// // @LoadBalancerClient(name = "course-service", configuration = LoadBalancerConfiguration.class)
// // public class WebConfig {

// //     @LoadBalanced
// //     @Bean
// //     public RestTemplate restTemplate() {
// //         return new RestTemplate();
// //     }
// // }

// package com.example.paymentservice.Configuration;

// import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.client.RestTemplate;
// import org.springframework.cloud.client.loadbalancer.LoadBalanced;

// @Configuration
// // @LoadBalancerClient(name = "PAYMENT-SERVICE", configuration = LoadBalancerConfiguration.class)
// public class WebConfig {

//     @LoadBalanced
//     @Bean
//     public RestTemplate restTemplate() {
//         return new RestTemplate();
//     }
// }


package com.example.paymentservice.Configuration;   // ← adjust package to match yours

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class RestTemplateConfig {

    /**
     * A single, Load‑Balanced RestTemplate that automatically forwards the
     * caller’s Authorization header to downstream services.
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {

        RestTemplate rt = new RestTemplate();

        rt.getInterceptors().add((request, body, execution) -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attrs != null) {
                HttpServletRequest incoming = attrs.getRequest();
                String authHeader = incoming.getHeader(HttpHeaders.AUTHORIZATION);
                if (authHeader != null) {
                    request.getHeaders().add(HttpHeaders.AUTHORIZATION, authHeader);
                }
            }

            return execution.execute(request, body);
        });

        return rt;
    }
}
