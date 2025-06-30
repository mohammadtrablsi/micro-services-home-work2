package com.example.paymentservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Autowired
    private UserRepository userRepository;

    // Check if the user has enough balance to pay for the course
    public boolean checkPaymentStatus(Long userId, Double coursePrice) {
        User user = userRepository.findById(userId).orElse(null);
        
        if (user != null && user.getWalletBalance() >= coursePrice) {
            return true;  // User has enough balance
        }
        
        return false;  // Not enough balance
    }

    // Process the payment: Deduct from user account and update balance
    public boolean processPayment(Long userId, Double coursePrice) {
        User user = userRepository.findById(userId).orElse(null);
        
        if (user != null && user.getWalletBalance() >= coursePrice) {
            // Deduct from the user's balance
            user.setWalletBalance(user.getWalletBalance() - coursePrice);
            userRepository.save(user);  // Save the updated user

            return true;  // Payment processed
        }
        
        return false;  // Insufficient funds
    }
}
