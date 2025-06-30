package com.example.userservice.security;

import org.springframework.stereotype.Component;
import com.example.userservice.entity.User;  // Import User class
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
// @Component
// public class JwtUtil {
//     private final String SECRET = "mySecretKey";

//     public String generateToken(User user) {
//         return Jwts.builder()
//                 .setSubject(user.getUsername())
//                 .claim("role", user.getRole().getName())
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
//                 .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256)
//                 .compact();
//     }
// }
@Component
public class JwtUtil {
    private final String SECRET = "mySecretKey";

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId())) // 👈 وضع الـ ID كـ Subject
                .claim("username", user.getUsername())     // 👈 إضافة الاسم كـ claim
                .claim("role", user.getRole().getName())   // 👈 الدور
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 يوم
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
}
