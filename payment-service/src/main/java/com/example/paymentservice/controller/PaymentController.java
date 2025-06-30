package com.example.paymentservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/process/{learnerId}/{coursePrice}/{courseId}")
    public ResponseEntity<?> processPayment(
            @PathVariable Long learnerId,
            @PathVariable Double coursePrice,
            @PathVariable Long courseId
    ) {
        // تحقق من أن المستخدم لديه رصيد كافي
        boolean hasSufficientBalance = checkBalance(learnerId, coursePrice);
        if (!hasSufficientBalance) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Insufficient funds");
        }

        // خصم المبلغ من رصيد المتعلم
        deductBalance(learnerId, coursePrice);

        // إضافة سجل الدفع
        savePaymentTransaction(learnerId, courseId, coursePrice);

        return ResponseEntity.ok("Payment successful");
    }

    private boolean checkBalance(Long learnerId, Double coursePrice) {
        User user = userRepository.findById(learnerId).orElse(null);
        return user != null && user.getWalletBalance() >= coursePrice;
    }

    private void deductBalance(Long learnerId, Double coursePrice) {
        User user = userRepository.findById(learnerId).orElse(null);
        if (user != null) {
            user.setWalletBalance(user.getWalletBalance() - coursePrice);
            userRepository.save(user);
        }
    }

    private void savePaymentTransaction(Long learnerId, Long courseId, Double coursePrice) {
        // إنشاء سجل دفع جديد
        PaymentTransaction transaction = new PaymentTransaction(learnerId, courseId, coursePrice);
        paymentTransactionRepository.save(transaction);
    }
}
