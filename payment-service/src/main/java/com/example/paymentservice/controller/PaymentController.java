// package com.example.paymentservice.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.example.paymentservice.entity.PaymentTransaction;

// import org.springframework.http.HttpStatus;

// @RestController
// @RequestMapping("/payment")
// public class PaymentController {

//     @Autowired
//     private UserRepository userRepository;

//     @PostMapping("/process/{learnerId}/{coursePrice}/{courseId}")
//     public ResponseEntity<?> processPayment(
//             @PathVariable Long learnerId,
//             @PathVariable Double coursePrice,
//             @PathVariable Long courseId
//     ) {
//         // تحقق من أن المستخدم لديه رصيد كافي
//         boolean hasSufficientBalance = checkBalance(learnerId, coursePrice);
//         if (!hasSufficientBalance) {
//             return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Insufficient funds");
//         }

//         // خصم المبلغ من رصيد المتعلم
//         deductBalance(learnerId, coursePrice);

//         // إضافة سجل الدفع
//         savePaymentTransaction(learnerId, courseId, coursePrice);

//         return ResponseEntity.ok("Payment successful");
//     }

//     private boolean checkBalance(Long learnerId, Double coursePrice) {
//         User user = userRepository.findById(learnerId).orElse(null);
//         return user != null && user.getWalletBalance() >= coursePrice;
//     }

//     private void deductBalance(Long learnerId, Double coursePrice) {
//         User user = userRepository.findById(learnerId).orElse(null);
//         if (user != null) {
//             user.setWalletBalance(user.getWalletBalance() - coursePrice);
//             userRepository.save(user);
//         }
//     }

//     private void savePaymentTransaction(Long learnerId, Long courseId, Double coursePrice) {
//         // إنشاء سجل دفع جديد
//         PaymentTransaction transaction = new PaymentTransaction(learnerId, courseId, coursePrice);
//         paymentTransactionRepository.save(transaction);
//     }
// }
package com.example.paymentservice.controller;

import com.example.paymentservice.entity.PaymentTransaction;
import com.example.paymentservice.repository.PaymentTransactionRepository;
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

    // 👇 Use the service name registered in Eureka or your service discovery tool
    private static final String USER_SERVICE_BASE_URL = "http://user-service/users";

    @PostMapping("/process/{learnerId}/{coursePrice}/{courseId}")
    public ResponseEntity<?> processPayment(
            @PathVariable Long learnerId,
            @PathVariable Double coursePrice,
            @PathVariable Long courseId
    ) {
        // 1. Check balance
        String balanceUrl = USER_SERVICE_BASE_URL + "/" + learnerId + "/balance";
        ResponseEntity<Double> balanceResponse = restTemplate.getForEntity(balanceUrl, Double.class);

        if (!balanceResponse.getStatusCode().is2xxSuccessful() || balanceResponse.getBody() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not retrieve user balance");
        }

        Double balance = balanceResponse.getBody();
        if (balance < coursePrice) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Insufficient funds");
        }

        // 2. Deduct balance
        String deductUrl = USER_SERVICE_BASE_URL + "/" + learnerId + "/deduct?amount=" + coursePrice;
        try {
            restTemplate.postForEntity(deductUrl, null, Void.class);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to deduct balance: " + e.getMessage());
        }

        // 3. Save payment transaction
        PaymentTransaction transaction = new PaymentTransaction(learnerId, courseId, coursePrice);
        paymentTransactionRepository.save(transaction);

        return ResponseEntity.ok("Payment successful");
    }
}
