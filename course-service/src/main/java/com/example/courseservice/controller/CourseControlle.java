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
        // تحقق من وجود الدورة
        Course course = courseRepository.findById(subscriptionRequest.getCourseId()).orElse(null);
        if (course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }

        // التحقق من الدفع
        boolean paymentStatus = checkPaymentStatus(learnerId, course.getPrice());
        if (!paymentStatus) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Payment required");
        }

        // عملية الدفع إذا كانت الحالة صحيحة
        processPayment(learnerId, course.getPrice(), course);

        // إضافة المتعلم إلى الدورة
        // مثلا: إضافة الدورة إلى قائمة الدورات المسجلة للمتعلم
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully subscribed to course");
    }

    // تحقق من حالة الدفع عبر RestTemplate إلى خدمة الدفع
    private boolean checkPaymentStatus(Long learnerId, Double coursePrice) {
        // هنا يمكننا استدعاء API في خدمة الدفع للتحقق من حالة الدفع
        ResponseEntity<Boolean> paymentResponse = restTemplate.exchange(
            "http://payment-service/payment/check/" + learnerId + "/" + coursePrice,
            HttpMethod.GET,
            null,
            Boolean.class
        );
        return paymentResponse.getBody();
    }

    // معالجة الدفع
    private void processPayment(Long learnerId, Double coursePrice, Course course) {
        restTemplate.exchange(
            "http://payment-service/payment/process/" + learnerId + "/" + coursePrice + "/" + course.getId(),
            HttpMethod.POST,
            null,
            Void.class
        );
    }

   // In CourseService (or any downstream service)
   @GetMapping("/courses")
   public ResponseEntity<?> getSubscribedCoursesAndTestResults(
        @RequestHeader("X-User-Id") Long userId,  
        @RequestHeader("X-User-Role") String role) {

    if (!"LEARNER".equalsIgnoreCase(role)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only learners can access their courses and results");
    }

    // Fetch the courses the user is subscribed to
    List<Course> courses = courseRepository.findBySubscribedUsersId(userId); // Use userId to fetch courses

    if (courses.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No courses found for this user");
    }

    List<Map<String, Object>> coursesWithResults = new ArrayList<>();

    // Fetch the test results for each course
    for (Course course : courses) {
        Map<String, Object> courseWithResults = new HashMap<>();
        courseWithResults.put("course", course);
        
        // Fetch the test results for the user in this course
        List<TestResult> results = testResultRepository.findByUserIdAndTestCourseId(userId, course.getId());
        courseWithResults.put("results", results);
        
        coursesWithResults.add(courseWithResults);
    }

    return ResponseEntity.ok(coursesWithResults);
}


}
