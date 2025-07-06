package com.example.paymentservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Service
public class PaymentService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String USER_SERVICE_BASE_URL = "http://user-service/users";

    // Check if the user has enough balance to pay for the course
    public boolean checkPaymentStatus(Long userId, Double coursePrice) {
        try {
            String url = USER_SERVICE_BASE_URL + "/" + userId + "/balance";
            ResponseEntity<Double> response = restTemplate.getForEntity(url, Double.class);
            Double balance = response.getBody();

            return balance != null && balance >= coursePrice;
        } catch (Exception e) {
            return false; // Could not check balance
        }
    }

    // Process the payment: Deduct from user account by calling user-service
    public boolean processPayment(Long userId, Double coursePrice) {
        try {
            String url = USER_SERVICE_BASE_URL + "/" + userId + "/deduct?amount=" + coursePrice;
            restTemplate.postForEntity(url, null, Void.class);
            return true;
        } catch (Exception e) {
            return false; // Could not deduct
        }
    }
}
