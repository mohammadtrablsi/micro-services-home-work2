package com.example.course_service.controller;

import com.example.course_service.entity.Course;
import com.example.course_service.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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

    
    @GetMapping("/trainer/by-name")
    @CircuitBreaker(name = "userServiceCircuitBreaker", fallbackMethod = "getTrainerFallback")
    @Retry(name = "userServiceRetry")
    @TimeLimiter(name = "userServiceTimeout", fallbackMethod = "getTrainerFallback")
    public ResponseEntity<?> getCoursesByTrainerName(
            @RequestParam String name,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized");
        }

        // الاتصال بخدمة المستخدم لجلب ID المدرّب حسب الاسم
        ResponseEntity<UserDTO> userRes = restTemplate.getForEntity(
                "http://USER-SERVICE/users/name/" + name,
                UserDTO.class
        );

        if (!userRes.getStatusCode().is2xxSuccessful() || userRes.getBody() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainer not found");
        }

        Long trainerId = userRes.getBody().getId();
        List<Course> courses = repo.findByTrainerId(trainerId);
        return ResponseEntity.ok(courses);
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

        // API للموافقة على الدورة من قبل الـ admin
    @PutMapping("/approve/{courseId}")
    public ResponseEntity<Course> approveCourse(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);  // فقط المسؤول يمكنه الموافقة
        }

        Course course = repo.findById(courseId).orElse(null);
        if (course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);  // الدورة غير موجودة
        }

        course.setApproved(true);  // تعيين الدورة كموافقة
        Course approvedCourse = repo.save(course);
        return ResponseEntity.ok(approvedCourse);
    }
        // عرض جميع الدورات
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = repo.findAll();
        return ResponseEntity.ok(courses);
    }

     // API للاشتراك في دورة
@PostMapping("/subscribe")
public ResponseEntity<?> subscribeToCourse(
    @RequestBody CourseSubscriptionRequest subscriptionRequest, 
    @RequestHeader("X-User-Id") Long learnerId
) {
    // 1. تحقق من وجود الدورة
    Course course = repo.findById(subscriptionRequest.getCourseId()).orElse(null);
    if (course == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
    }

    try {
        // 2. التحقق من الدفع باستخدام CompletableFuture + Resilience4j
        boolean paymentStatus = checkPaymentStatus(learnerId, course.getPrice()).join();
        if (!paymentStatus) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Payment required");
        }

        // 3. تنفيذ عملية الدفع
        processPayment(learnerId, course.getPrice(), course).join();

        // 4. من المفترض هنا إضافة المتعلم إلى الدورة (مثلاً في جدول أو علاقة اشتراك)
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully subscribed to course");

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Subscription failed due to internal error");
    }
}

    // تحقق من حالة الدفع عبر RestTemplate إلى خدمة الدفع
  @CircuitBreaker(name = "paymentServiceCB", fallbackMethod = "checkPaymentStatusFallback")
@Retry(name = "paymentServiceRetry")
@TimeLimiter(name = "paymentServiceTimeout", fallbackMethod = "checkPaymentStatusFallback")
private CompletableFuture<Boolean> checkPaymentStatus(Long learnerId, Double coursePrice) {
    return CompletableFuture.supplyAsync(() -> {
        ResponseEntity<Boolean> paymentResponse = restTemplate.exchange(
            "http://payment-service/payment/check/" + learnerId + "/" + coursePrice,
            HttpMethod.GET,
            null,
            Boolean.class
        );
        return Boolean.TRUE.equals(paymentResponse.getBody());
    });
}

// Fallback for checkPaymentStatus
private CompletableFuture<Boolean> checkPaymentStatusFallback(Long learnerId, Double coursePrice, Throwable t) {
    return CompletableFuture.completedFuture(false); // return false when service fails
}


   @CircuitBreaker(name = "paymentServiceCB", fallbackMethod = "processPaymentFallback")
@Retry(name = "paymentServiceRetry")
@TimeLimiter(name = "paymentServiceTimeout", fallbackMethod = "processPaymentFallback")
private CompletableFuture<Void> processPayment(Long learnerId, Double coursePrice, Course course) {
    return CompletableFuture.runAsync(() -> {
        restTemplate.exchange(
            "http://payment-service/payment/process/" + learnerId + "/" + coursePrice + "/" + course.getId(),
            HttpMethod.POST,
            null,
            Void.class
        );
    });
}

// Fallback for processPayment
private CompletableFuture<Void> processPaymentFallback(Long learnerId, Double coursePrice, Course course, Throwable t) {
    return CompletableFuture.completedFuture(null); // silently continue or log error
}


//    // In CourseService (or any downstream service)
//    @GetMapping("/courses")
//    public ResponseEntity<?> getSubscribedCoursesAndTestResults(
//         @RequestHeader("X-User-Id") Long userId,  
//         @RequestHeader("X-User-Role") String role) {

//     if (!"LEARNER".equalsIgnoreCase(role)) {
//         return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only learners can access their courses and results");
//     }

//     // Fetch the courses the user is subscribed to
//     List<Course> courses = courseRepository.findBySubscribedUsersId(userId); // Use userId to fetch courses

//     if (courses.isEmpty()) {
//         return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No courses found for this user");
//     }

//     List<Map<String, Object>> coursesWithResults = new ArrayList<>();

//     // Fetch the test results for each course
//     for (Course course : courses) {
//         Map<String, Object> courseWithResults = new HashMap<>();
//         courseWithResults.put("course", course);
        
//         // Fetch the test results for the user in this course
//         List<TestResult> results = testResultRepository.findByUserIdAndTestCourseId(userId, course.getId());
//         courseWithResults.put("results", results);
        
//         coursesWithResults.add(courseWithResults);
//     }

//     return ResponseEntity.ok(coursesWithResults);
// }
@GetMapping("/my-courses-with-results")
public ResponseEntity<?> getSubscribedCoursesAndTestResults(
        @RequestHeader("X-User-Id") Long userId,
        @RequestHeader("X-User-Role") String role
) {
    if (!"LEARNER".equalsIgnoreCase(role)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only learners can access this endpoint");
    }

    List<Course> courses = repo.findBySubscribedUsersId(userId);
    if (courses.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No subscribed courses found");
    }

    List<Map<String, Object>> coursesWithResults = new ArrayList<>();

    for (Course course : courses) {
        Map<String, Object> entry = new HashMap<>();
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
    log.error("Failed to fetch results from assessment service", t);
    return List.of("Assessment service is currently unavailable");
}


public ResponseEntity<?> getTrainerFallback(String name, String role, Throwable throwable) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("User service is not available now. Please try again later.");
}
public ResponseEntity<?> paymentServiceFallback(Long learnerId, Double price, Throwable throwable) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("Payment service is currently unavailable. Please try again later.");
}
}
