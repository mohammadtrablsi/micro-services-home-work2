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

}
