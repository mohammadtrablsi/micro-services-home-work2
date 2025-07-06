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




// package com.example.paymentservice.Configuration;

// // // // package com.example.courseservice.Configuration;

// // // import org.springframework.cloud.client.loadbalancer.LoadBalanced;
// // // import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
// // // import org.springframework.context.annotation.Bean;
// // // import org.springframework.context.annotation.Configuration;
// // // import org.springframework.web.client.RestTemplate;

// // // @Configuration
// // // @LoadBalancerClient(name = "course-service", configuration = LoadBalancerConfiguration.class)
// // // public class WebConfig {

// // //     @LoadBalanced
// // //     @Bean
// // //     public RestTemplate restTemplate() {
// // //         return new RestTemplate();
// // //     }
// // // }

// // package com.example.paymentservice.Configuration;

// // import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
// // import org.springframework.context.annotation.Bean;
// // import org.springframework.context.annotation.Configuration;
// // import org.springframework.web.client.RestTemplate;
// // import org.springframework.cloud.client.loadbalancer.LoadBalanced;

// // @Configuration
// // // @LoadBalancerClient(name = "PAYMENT-SERVICE", configuration = LoadBalancerConfiguration.class)
// // public class WebConfig {

// //     @LoadBalanced
// //     @Bean
// //     public RestTemplate restTemplate() {
// //         return new RestTemplate();
// //     }
// // }


// package com.example.paymentservice.Configuration;   // ← adjust package to match yours

// import jakarta.servlet.http.HttpServletRequest;
// import org.springframework.cloud.client.loadbalancer.LoadBalanced;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.HttpHeaders;
// import org.springframework.web.client.RestTemplate;
// import org.springframework.web.context.request.RequestContextHolder;
// import org.springframework.web.context.request.ServletRequestAttributes;

// @Configuration
// public class RestTemplateConfig {

//     /**
//      * A single, Load‑Balanced RestTemplate that automatically forwards the
//      * caller’s Authorization header to downstream services.
//      */
//     @Bean
//     @LoadBalanced
//     public RestTemplate restTemplate() {

//         RestTemplate rt = new RestTemplate();

//         rt.getInterceptors().add((request, body, execution) -> {
//             ServletRequestAttributes attrs =
//                     (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

//             if (attrs != null) {
//                 HttpServletRequest incoming = attrs.getRequest();
//                 String authHeader = incoming.getHeader(HttpHeaders.AUTHORIZATION);
//                 if (authHeader != null) {
//                     request.getHeaders().add(HttpHeaders.AUTHORIZATION, authHeader);
//                 }
//             }

//             return execution.execute(request, body);
//         });

//         return rt;
//     }
// }package com.example.paymentservice.config;      // غيّر المسار إن لزم

// import jakarta.servlet.http.HttpServletRequest;
// import org.springframework.beans.factory.ObjectProvider;
// import org.springframework.cloud.client.ServiceInstance;
// import org.springframework.cloud.client.loadbalancer.LoadBalanced;
// import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
// import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
// import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
// import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.core.env.Environment;
// import org.springframework.http.HttpHeaders;
// import org.springframework.web.client.RestTemplate;
// import org.springframework.web.context.request.RequestContextHolder;
// import org.springframework.web.context.request.ServletRequestAttributes;

// @Configuration
// public class RestTemplateConfig {

//     /*------------------------------------------------------------
//      * 1) RestTemplate مُحمَّل بإعداد الـLoadBalancer
//      *-----------------------------------------------------------*/
//     @Bean
//     @LoadBalanced                   // ⇦ Eureka + LB
//     public RestTemplate restTemplate() {

//         RestTemplate rt = new RestTemplate();

//         /* تمرير هيدر Authorization لكل الخدمات اللاحقة */
//         rt.getInterceptors().add((request, body, execution) -> {
//             ServletRequestAttributes attrs =
//                     (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

//             if (attrs != null) {
//                 HttpServletRequest incoming = attrs.getRequest();
//                 String auth = incoming.getHeader(HttpHeaders.AUTHORIZATION);
//                 if (auth != null) {
//                     request.getHeaders().add(HttpHeaders.AUTHORIZATION, auth);
//                 }
//             }
//             return execution.execute(request, body);
//         });

//         return rt;
//     }

//     /*------------------------------------------------------------
//      * 2) موازِن التحميل Round‑Robin بدلاً من Random
//      *-----------------------------------------------------------*/
//     @Bean
//     public ReactorLoadBalancer<ServiceInstance> roundRobinLoadBalancer(
//             Environment env,
//             ObjectProvider<ServiceInstanceListSupplier> supplier) {

//         /*
//          *  LoadBalancerClientFactory.PROPERTY_NAME يحمل اسم
//          *  الخدمة المطلوب موازنتها (يُمرَّر آلياً من Spring
//          *  حسب الـ serviceId الموجود في الـ URI).
//          */
//         String serviceId = env.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
//         return new RoundRobinLoadBalancer(supplier, serviceId);
//     }
// }


