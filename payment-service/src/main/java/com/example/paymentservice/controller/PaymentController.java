package com.example.paymentservice.controller;

import com.example.paymentservice.entity.PaymentTransaction;
import com.example.paymentservice.repository.PaymentTransactionRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/payment")
public class PaymentController {
     @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
     private static final String USER_SERVICE_BASE_URL = "http://USER-SERVICE/auth/users";
     

    /* ---------------- check balance ---------------- */
    @GetMapping("/check/{learnerId}/{coursePrice}")
    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "checkBalanceFallback")
    @Retry(name = "userServiceRetry")
    public ResponseEntity<Boolean> checkPaymentEligibility(
            @PathVariable Long learnerId,
            @PathVariable Double coursePrice) {

        String url = "http://USER-SERVICE/auth/users/" + learnerId + "/balance";

        
        ResponseEntity<Double> res = restTemplate.getForEntity(url, Double.class);
        double balance = res.getBody();
        return ResponseEntity.ok(balance >= coursePrice);
    }

    
    public ResponseEntity<Boolean> checkBalanceFallback(Long learnerId,
                                                        Double coursePrice,
                                                        Throwable ex) {
        return ResponseEntity.ok(false);
    }

    /* ---------------- process payment -------------- */
    @PostMapping("/process/{learnerId}/{coursePrice}/{courseId}")
    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "deductServiceDown")
    @Retry(name = "userServiceRetry")
    public ResponseEntity<?> processPayment(
            @PathVariable Long learnerId,
            @PathVariable Double coursePrice,
            @PathVariable Long courseId) {

        String balURL = USER_SERVICE_BASE_URL + "/" + learnerId + "/balance";
        double balance = restTemplate.getForEntity(balURL, Double.class).getBody();

        if (balance < coursePrice)
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Insufficient funds");

        String deductURL = USER_SERVICE_BASE_URL + "/" + learnerId + "/deduct?amount=" + coursePrice;
        restTemplate.postForEntity(deductURL, null, Void.class);   // قد يرمي استثناء

        paymentTransactionRepository.save(
                new PaymentTransaction(learnerId, courseId, coursePrice));

        return ResponseEntity.ok("Payment successful");
    }

   
    public ResponseEntity<?> deductServiceDown(Long learnerId,
                                               Double coursePrice,
                                               Long courseId,
                                               Throwable ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                             .body("USER‑SERVICE غير متاح حالياً، لم يتم تنفيذ عملية الدفع.");
    }
}
