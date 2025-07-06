// // // package com.example.paymentservice.controller;

// // // import org.springframework.beans.factory.annotation.Autowired;
// // // import org.springframework.http.ResponseEntity;
// // // import org.springframework.web.bind.annotation.PathVariable;
// // // import org.springframework.web.bind.annotation.PostMapping;
// // // import org.springframework.web.bind.annotation.RequestMapping;
// // // import org.springframework.web.bind.annotation.RestController;

// // // import com.example.paymentservice.entity.PaymentTransaction;

// // // import org.springframework.http.HttpStatus;

// // // @RestController
// // // @RequestMapping("/payment")
// // // public class PaymentController {

// // //     @Autowired
// // //     private UserRepository userRepository;

// // //     @PostMapping("/process/{learnerId}/{coursePrice}/{courseId}")
// // //     public ResponseEntity<?> processPayment(
// // //             @PathVariable Long learnerId,
// // //             @PathVariable Double coursePrice,
// // //             @PathVariable Long courseId
// // //     ) {
// // //         // تحقق من أن المستخدم لديه رصيد كافي
// // //         boolean hasSufficientBalance = checkBalance(learnerId, coursePrice);
// // //         if (!hasSufficientBalance) {
// // //             return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Insufficient funds");
// // //         }

// // //         // خصم المبلغ من رصيد المتعلم
// // //         deductBalance(learnerId, coursePrice);

// // //         // إضافة سجل الدفع
// // //         savePaymentTransaction(learnerId, courseId, coursePrice);

// // //         return ResponseEntity.ok("Payment successful");
// // //     }

// // //     private boolean checkBalance(Long learnerId, Double coursePrice) {
// // //         User user = userRepository.findById(learnerId).orElse(null);
// // //         return user != null && user.getWalletBalance() >= coursePrice;
// // //     }

// // //     private void deductBalance(Long learnerId, Double coursePrice) {
// // //         User user = userRepository.findById(learnerId).orElse(null);
// // //         if (user != null) {
// // //             user.setWalletBalance(user.getWalletBalance() - coursePrice);
// // //             userRepository.save(user);
// // //         }
// // //     }

// // //     private void savePaymentTransaction(Long learnerId, Long courseId, Double coursePrice) {
// // //         // إنشاء سجل دفع جديد
// // //         PaymentTransaction transaction = new PaymentTransaction(learnerId, courseId, coursePrice);
// // //         paymentTransactionRepository.save(transaction);
// // //     }
// // // }
// // package com.example.paymentservice.controller;

// // import com.example.paymentservice.entity.PaymentTransaction;
// // import com.example.paymentservice.repository.PaymentTransactionRepository;
// // import org.springframework.beans.factory.annotation.Autowired;
// // import org.springframework.http.*;
// // import org.springframework.web.bind.annotation.*;
// // import org.springframework.web.client.RestTemplate;

// // @RestController
// // @RequestMapping("/payment")
// // public class PaymentController {

// //     @Autowired
// //     private RestTemplate restTemplate;

// //     @Autowired
// //     private PaymentTransactionRepository paymentTransactionRepository;

// //     // 👇 Use the service name registered in Eureka or your service discovery tool
// //     private static final String USER_SERVICE_BASE_URL = "http://user-service/users";

// //     @PostMapping("/process/{learnerId}/{coursePrice}/{courseId}")
// //     public ResponseEntity<?> processPayment(
// //             @PathVariable Long learnerId,
// //             @PathVariable Double coursePrice,
// //             @PathVariable Long courseId
// //     ) {
// //         // 1. Check balance
// //         String balanceUrl = USER_SERVICE_BASE_URL + "/" + learnerId + "/balance";
// //         ResponseEntity<Double> balanceResponse = restTemplate.getForEntity(balanceUrl, Double.class);

// //         if (!balanceResponse.getStatusCode().is2xxSuccessful() || balanceResponse.getBody() == null) {
// //             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not retrieve user balance");
// //         }

// //         Double balance = balanceResponse.getBody();
// //         if (balance < coursePrice) {
// //             return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Insufficient funds");
// //         }

// //         // 2. Deduct balance
// //         String deductUrl = USER_SERVICE_BASE_URL + "/" + learnerId + "/deduct?amount=" + coursePrice;
// //         try {
// //             restTemplate.postForEntity(deductUrl, null, Void.class);
// //         } catch (Exception e) {
// //             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to deduct balance: " + e.getMessage());
// //         }

// //         // 3. Save payment transaction
// //         PaymentTransaction transaction = new PaymentTransaction(learnerId, courseId, coursePrice);
// //         paymentTransactionRepository.save(transaction);

// //         return ResponseEntity.ok("Payment successful");
// //     }
// // }
// package com.example.paymentservice.controller;

// import com.example.paymentservice.entity.PaymentTransaction;
// import com.example.paymentservice.repository.PaymentTransactionRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.*;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.client.RestTemplate;

// /**
//  * REST API for handling payments.
//  *
//  * المسارات المتاحة:
//  *   GET  /payment/check/{learnerId}/{courseId}      ‑ هل دفع هذا المتعلم لهذه الدورة؟
//  *   POST /payment/process/{learnerId}/{amount}/{courseId}  ‑ تنفيذ الدفع وتسجيله
//  */
// @RestController
// @RequestMapping("/payment")
// public class PaymentController {

//     /** RestTemplate مزيَّن في Bean آخر بـ @LoadBalanced ليستخدم Eureka */
//     @Autowired
//     private RestTemplate restTemplate;

//     @Autowired
//     private PaymentTransactionRepository paymentRepo;

//     // اسم خدمة المستخدم كما هو مسجَّل في Eureka
//     private static final String USER_SERVICE_BASE = "http://USER-SERVICE/users";

//     /* -----------------------------------------------------------
//        1) التحقق: هل سبق دفع المتعلّم ثمن هذه الدورة؟
//        ----------------------------------------------------------- */
//     @GetMapping("/check/{learnerId}/{courseId}")
//     public ResponseEntity<Boolean> checkPayment(
//             @PathVariable Long learnerId,
//             @PathVariable Long courseId) {

//         boolean alreadyPaid =
//                 paymentRepo.existsByLearnerIdAndCourseId(learnerId, courseId);

//         return ResponseEntity.ok(alreadyPaid);
//     }

//     /* -----------------------------------------------------------
//        2) تنفيذ الدفع وتسجيل المعاملة
//        ----------------------------------------------------------- */
//     @PostMapping("/process/{learnerId}/{amount}/{courseId}")
//     public ResponseEntity<?> processPayment(
//             @PathVariable Long learnerId,
//             @PathVariable Double amount,
//             @PathVariable Long courseId) {

//         /* 2‑A: التحقق من رصيد المستخدم في USER‑SERVICE */
//         String balanceUrl = USER_SERVICE_BASE + "/" + learnerId + "/balance";
//         ResponseEntity<Double> balRes =
//                 restTemplate.getForEntity(balanceUrl, Double.class);

//         if (!balRes.getStatusCode().is2xxSuccessful() || balRes.getBody() == null) {
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                     .body("Could not retrieve user balance");
//         }

//         double balance = balRes.getBody();
//         if (balance < amount) {
//             return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
//                     .body("Insufficient funds");
//         }

//         /* 2‑B: خصم الرصيد */
//         String deductUrl = USER_SERVICE_BASE + "/" + learnerId + "/deduct?amount=" + amount;
//         try {
//             restTemplate.postForEntity(deductUrl, null, Void.class);
//         } catch (Exception ex) {
//             return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
//                     .body("Failed to deduct balance: " + ex.getMessage());
//         }

//         /* 2‑C: تسجيل معاملة الدفع */
//         PaymentTransaction tx = new PaymentTransaction(learnerId, courseId, amount);
//         paymentRepo.save(tx);

//         return ResponseEntity.ok("Payment successful");
//     }
// }
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

// @RestController
// @RequestMapping("/payment")
// public class PaymentController {

//     @Autowired
//     private RestTemplate restTemplate;

//     @Autowired
//     private PaymentTransactionRepository paymentTransactionRepository;

//     private static final String USER_SERVICE_BASE_URL = "http://USER-SERVICE/auth/users";

// @GetMapping("/check/{learnerId}/{coursePrice}")
// public ResponseEntity<Boolean> checkPaymentEligibility(
//         @PathVariable Long learnerId,
//         @PathVariable Double coursePrice) {
//     try {
//         String balanceUrl = "http://USER-SERVICE/users/" + learnerId + "/balance";
//         ResponseEntity<Double> balanceResponse = restTemplate.getForEntity(balanceUrl, Double.class);

//         if (!balanceResponse.getStatusCode().is2xxSuccessful() || balanceResponse.getBody() == null) {
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
//         }

//         Double balance = balanceResponse.getBody();
//         return ResponseEntity.ok(balance >= coursePrice);

//     } catch (Exception e) {
//         // Optional: log the error
//         e.printStackTrace();
//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
//     }
// }
/* right code
@GetMapping("/check/{learnerId}/{coursePrice}")
@CircuitBreaker(name = "userServiceCB", fallbackMethod = "checkBalanceFallback")
@Retry(name = "userServiceRetry")
// @TimeLimiter(name = "userServiceTimeout", fallbackMethod = "checkBalanceFallback")
public ResponseEntity<Boolean> checkPaymentEligibility(
        @PathVariable Long learnerId,
        @PathVariable Double coursePrice) {

    String balanceUrl = "http://USER-SERVICE/auth/users/" + learnerId + "/balance";

    try {
        ResponseEntity<Double> balRes =
                restTemplate.getForEntity(balanceUrl, Double.class);

        if (!balRes.getStatusCode().is2xxSuccessful() || balRes.getBody() == null) {
            // مثلاً 404 أو 400
            return ResponseEntity.ok(false);   // ❌ لم نجد رصيد → لم يدفع
        }

        double balance = balRes.getBody();
        return ResponseEntity.ok(balance >= coursePrice);

    } catch (Exception ex) {
        ex.printStackTrace(); // أو سجّل في log
        // أي خطأ أثناء استدعاء خدمة المستخدم = لا يمكن التحقق → اعتبره لم يدفع
        return ResponseEntity.ok(false);
    }
}



    // ✅ This processes the actual payment
    
    @PostMapping("/process/{learnerId}/{coursePrice}/{courseId}")
@CircuitBreaker(name = "userServiceCB", fallbackMethod = "deductServiceDown")
@Retry(name = "userServiceRetry")
// @TimeLimiter(name = "userServiceTimeout", fallbackMethod = "deductServiceDown")
    
    public ResponseEntity<?> processPayment(
            @PathVariable Long learnerId,
            @PathVariable Double coursePrice,
            @PathVariable Long courseId) {

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

        // 3. Save transaction
        PaymentTransaction transaction = new PaymentTransaction(learnerId, courseId, coursePrice);
        paymentTransactionRepository.save(transaction);

        return ResponseEntity.ok("Payment successful");
    }

    public CompletableFuture<ResponseEntity<Boolean>> checkBalanceFallback(Long learnerId, Double coursePrice, Throwable ex) {
    return CompletableFuture.completedFuture(ResponseEntity.ok(false));
}
public CompletableFuture<ResponseEntity<?>> deductServiceDown(Long learnerId, Double coursePrice, Long courseId, Throwable ex) {
    return CompletableFuture.completedFuture(
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                      .body("USER‑SERVICE غير متاح حالياً، لم يتم تنفيذ عملية الدفع.")
    );
}

}
right code*/

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

        // ❌ لا تلتقط الاستثناء هنا
        ResponseEntity<Double> res = restTemplate.getForEntity(url, Double.class);
        double balance = res.getBody();
        return ResponseEntity.ok(balance >= coursePrice);
    }

    /* fallback ↙︎ نفس نوع الإرجاع ونفس قائمة البراميتر زائد Throwable */
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

    /* fallback ↙︎ نفس التوقيع */
    public ResponseEntity<?> deductServiceDown(Long learnerId,
                                               Double coursePrice,
                                               Long courseId,
                                               Throwable ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                             .body("USER‑SERVICE غير متاح حالياً، لم يتم تنفيذ عملية الدفع.");
    }
}
