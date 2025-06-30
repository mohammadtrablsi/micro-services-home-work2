package com.example.apigateway;

// @Component
// public class JwtAuthFilter implements GlobalFilter {

//     private final String SECRET_KEY = "mySecretKey";

//     @Override
//     public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//         String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

//         if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//             exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//             return exchange.getResponse().setComplete();
//         }

//         String token = authHeader.substring(7); // remove "Bearer "
//         try {
//             Claims claims = Jwts.parserBuilder()
//                     .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
//                     .build()
//                     .parseClaimsJws(token)
//                     .getBody();

//             String userId = claims.getSubject(); // from .setSubject(userId)
//             String role = claims.get("role", String.class);

//             // Forward userId and role in headers to downstream services
//             ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
//                     .header("X-User-Id", userId)
//                     .header("X-User-Role", role)
//                     .build();

//             return chain.filter(exchange.mutate().request(mutatedRequest).build());

//         } catch (Exception e) {
//             exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//             return exchange.getResponse().setComplete();
//         }
//     }
// }
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.server.reactive.ServerHttpRequest;


@Component
public class JwtAuthFilter implements GlobalFilter {

    private final String SECRET_KEY = "mySecretKey";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7); // remove "Bearer "
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.getSubject(); // من setSubject(userId)
            String role = claims.get("role", String.class);
            String username = claims.get("username", String.class); // ✅ إضافته

            // تمرير المعلومات للخدمات
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role)
                    .header("X-Username", username) // ✅ تمريره مع الهيدر
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
