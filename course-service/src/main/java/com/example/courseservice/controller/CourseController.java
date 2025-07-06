package com.example.courseservice.controller;

import com.example.courseservice.dto.CourseSubscriptionRequest;
import com.example.courseservice.dto.UserDTO;
import com.example.courseservice.entity.Course;
import com.example.courseservice.repository.CourseRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseRepository repo;

    @Autowired
    private RestTemplate restTemplate;

    public CourseController(CourseRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/trainer/me")
    public ResponseEntity<?> getMyCourses(
            @RequestHeader("X-User-Id") Long trainerId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!"TRAINER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized");
        }

        List<Course> courses = repo.findByTrainerId(trainerId);
        return ResponseEntity.ok(courses);
    }

@GetMapping("/trainer/byname")
@CircuitBreaker(name = "userServiceCircuitBreaker", fallbackMethod = "getTrainerFallback")
@Retry(name = "userServiceRetry")
// @TimeLimiter(name = "userServiceTimeout", fallbackMethod = "getTrainerFallback")
public CompletableFuture<ResponseEntity<?>> getCoursesByTrainerName(
        @RequestParam String name,
        @RequestHeader("X-User-Role") String role) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized"));
        }

        // Simulate async call to another service (RestTemplate call can be async)
        return CompletableFuture.supplyAsync(() -> {
            // Assume you have a method that gets the user from user-service
            ResponseEntity<UserDTO> userRes = restTemplate.getForEntity(
                    "http://USER-SERVICE/auth/users/name/" + name, 
                    UserDTO.class
            );

            if (!userRes.getStatusCode().is2xxSuccessful() || userRes.getBody() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainer not found");
            }

            Long trainerId = userRes.getBody().getId();
            List<Course> courses = repo.findByTrainerId(trainerId);
            return ResponseEntity.ok(courses);
        });
    }
    

    @PostMapping
    public ResponseEntity<?> createCourse(
            @RequestBody Course course,
            @RequestHeader("X-User-Id") Long trainerId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!"TRAINER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only trainers can create courses");
        }
        course.setApproved(false); 
        course.setTrainerId(trainerId);
        return ResponseEntity.ok(repo.save(course));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingCourses(
            @RequestHeader("X-User-Role") String role
    ) {
        // التحقق من أن الدور هو "ADMIN"
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can access pending courses");
        }

        // جلب الدورات التي لم تتم الموافقة عليها
        List<Course> pendingCourses = repo.findByApprovedFalse();
        return ResponseEntity.ok(pendingCourses);
    }

       
    @PutMapping("/approve/{courseId}")
    public ResponseEntity<Course> approveCourse(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);  
        }

        Course course = repo.findById(courseId).orElse(null);
        if (course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);  
        }

        course.setApproved(true); 
        Course approvedCourse = repo.save(course);
        return ResponseEntity.ok(approvedCourse);
    }
        
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = repo.findAll();
        return ResponseEntity.ok(courses);
    }


@PostMapping("/subscribe")
public ResponseEntity<?> subscribeToCourse(
        @RequestBody CourseSubscriptionRequest subscriptionRequest,
        @RequestHeader("X-User-Id") Long learnerId) {

    /* 1) التحقق من وجود الدورة */
    Course course = repo.findById(subscriptionRequest.getCourseId()).orElse(null);
    if (course == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
    }

    /* 2) التحقق من الدفع لدى payment‑service */
    boolean paymentStatus;
    try {
        paymentStatus = checkPaymentStatus(learnerId, course.getPrice()).join();
    } catch (Exception ex) {
        // فشل الاتصال بخدمة الدفع
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                         .body("PAYMENT‑SERVICE غير متاح حالياً، لا يمكن التحقق من حالة الدفع.");
    }

    if (!paymentStatus) {
        // لم يدفع المستخدم أو الرصيد غير كافٍ
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                             .body("Payment required");
    }

    /* 3) تنفيذ الدفع فعليًا (يخصم الرصيد ويسجل العملية) */
    try {
        processPayment(learnerId, course.getPrice(), course).join();
    } catch (Exception ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                             .body("Payment processing failed: " + ex.getMessage());
    }

    /* 4) إضافة المتعلّم إلى قائمة المشتركين إن لم يكن موجودًا بالفعل */
    if (!course.getSubscribedUserIds().contains(learnerId)) {
        course.getSubscribedUserIds().add(learnerId);
        repo.save(course);
    }

    return ResponseEntity.status(HttpStatus.CREATED)
                         .body("Successfully subscribed to course");
}


    // تحقق من حالة الدفع عبر RestTemplate إلى خدمة الدفع
  @CircuitBreaker(name = "paymentServiceCB", fallbackMethod = "checkPaymentStatusFallback")
@Retry(name = "paymentServiceRetry")
// @TimeLimiter(name = "paymentServiceTimeout", fallbackMethod = "checkPaymentStatusFallback")
private CompletableFuture<Boolean> checkPaymentStatus(Long learnerId, Double coursePrice) {
    return CompletableFuture.supplyAsync(() -> {
        ResponseEntity<Boolean> paymentResponse = restTemplate.exchange(
            "http://PAYMENT-SERVICE/payment/check/" + learnerId + "/" + coursePrice,
            HttpMethod.GET,
            null,
            Boolean.class
        );
        return Boolean.TRUE.equals(paymentResponse.getBody());
    });
}

public CompletableFuture<ResponseEntity<?>> checkPaymentStatusFallback(Long learnerId,
                                                                       Double coursePrice,
                                                                       Throwable t) {
    return CompletableFuture.completedFuture(
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("PAYEMENT‑SERVICE غير متاح حالياً، لا يمكن التحقق من حالة الدفع.")
    );
}


   @CircuitBreaker(name = "paymentServiceCB", fallbackMethod = "processPaymentFallback")
@Retry(name = "paymentServiceRetry")
// @TimeLimiter(name = "paymentServiceTimeout", fallbackMethod = "processPaymentFallback")
private CompletableFuture<Void> processPayment(Long learnerId, Double coursePrice, Course course) {
    return CompletableFuture.runAsync(() -> {
        restTemplate.exchange(
            "http://PAYMENT-SERVICE/payment/process/" + learnerId + "/" + coursePrice + "/" + course.getId(),
            HttpMethod.POST,
            null,
            Void.class
        );
    });
}

public CompletableFuture<ResponseEntity<?>> processPaymentFallback(Long learnerId,
                                                                    Double coursePrice,
                                                                    Course course,
                                                                    Throwable ex) {
    return CompletableFuture.completedFuture(
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("PAYEMENT‑SERVICE غير متاح حالياً، لم يتم تنفيذ عملية الدفع.")
    );
}

@GetMapping("/my-courses-with-results")
public ResponseEntity<?> getSubscribedCoursesAndTestResults(
        @RequestHeader("X-User-Id") Long userId,
        @RequestHeader("X-User-Role") String role
) {
    if (!"LEARNER".equalsIgnoreCase(role)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only learners can access this endpoint");
    }

    List<Course> courses = repo.findBySubscribedUserIdsContaining(userId); // if using Long list
    if (courses.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No subscribed courses found");
    }

    List<Map<String, Object>> coursesWithResults = new ArrayList<>();

    for (Course course : courses) {
        Map<String, Object> entry = new HashMap<String, Object>();
        entry.put("course", course);

        try {
            List<?> results = getResultsFromAssessment(userId, course.getId());
            entry.put("results", results);
        } catch (Exception e) {
            entry.put("results", "Failed to fetch test results");
        }

        coursesWithResults.add(entry);
    }

    return ResponseEntity.ok(coursesWithResults);
}
@GetMapping("/available")
public ResponseEntity<?> getAvailableCoursesForLearner(
        @RequestHeader("X-User-Id") Long learnerId,
        @RequestHeader("X-User-Role") String role
) {
    if (!"LEARNER".equalsIgnoreCase(role)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only learners can access this endpoint");
    }

    List<Course> allApprovedCourses = repo.findByApprovedTrue();
    List<Course> availableCourses = new ArrayList<>();

    for (Course course : allApprovedCourses) {
        if (!course.getSubscribedUserIds().contains(learnerId)) {
            availableCourses.add(course);
        }
    }

    return ResponseEntity.ok(availableCourses);
}

@GetMapping("/{courseId}")
public ResponseEntity<Boolean> checkCourseExists(@PathVariable Long courseId) {
    boolean exists = repo.existsById(courseId);
    return ResponseEntity.ok(exists);
}

@CircuitBreaker(name = "assessmentServiceCB", fallbackMethod = "assessmentServiceFallback")
@Retry(name = "assessmentServiceRetry")
public List<?> getResultsFromAssessment(Long userId, Long courseId) {
    String url = "http://ASSESSMENT-SERVICE/tests/results/user/" + userId + "/course/" + courseId;
    ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, null, List.class);
    return response.getBody();
}

public List<?> assessmentServiceFallback(Long userId, Long courseId, Throwable t) {
    // log.error("Failed to fetch results from assessment service", t);
    return List.of("Assessment service is currently unavailable");
}


public CompletableFuture<ResponseEntity<?>> getTrainerFallback(String name, String role, Throwable throwable) {
    return CompletableFuture.completedFuture(
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("User service is not available now. Please try again later.")
    );
}

public ResponseEntity<?> paymentServiceFallback(Long learnerId, Double price, Throwable throwable) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("Payment service is currently unavailable. Please try again later.");
}
}
