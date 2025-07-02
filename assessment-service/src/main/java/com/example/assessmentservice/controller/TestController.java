package com.example.assessmentservice.controller;

import com.example.assessmentservice.entity.Test;
import com.example.assessmentservice.entity.TestResult;
import com.example.assessmentservice.repository.TestRepository;
import com.example.assessmentservice.repository.TestResultRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/tests")
public class TestController {

    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;

    @Autowired
    private RestTemplate restTemplate;

    public TestController(TestRepository testRepository, TestResultRepository testResultRepository) {
        this.testRepository = testRepository;
        this.testResultRepository = testResultRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<Test> createTest(@RequestBody Test test, @RequestHeader("X-User-Role") String role) {
        if (!"TRAINER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);  // Only trainers can create tests
        }

        // check if the trainer is authorized (could be via user service or a different logic)
        // Here we just assume that the role check is sufficient
        testRepository.save(test);  // Save the test if the trainer role is valid
        return ResponseEntity.status(HttpStatus.CREATED).body(test);
    }
    @CircuitBreaker(name = "courseServiceCB", fallbackMethod = "courseServiceFallback")
    @Retry(name = "courseServiceRetry")
    @TimeLimiter(name = "courseServiceTimeout", fallbackMethod = "courseServiceFallback")
    @PostMapping("/addResult")
    public ResponseEntity<TestResult> addTestResult(@RequestBody TestResult result, @RequestHeader("X-User-Role") String role) {
        if (!"LEARNER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // Only learners can add results
        }

        // Check if the course exists (via CourseService)
        ResponseEntity<Boolean> courseExistsResponse = restTemplate.exchange(
                "http://COURSE-SERVICE/courses/" + result.getTest().getCourseId(), 
                HttpMethod.GET, 
                null, 
                Boolean.class
        );

        if (!courseExistsResponse.getStatusCode().is2xxSuccessful() || !courseExistsResponse.getBody()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Course not found
        }

        // Save the test result for the learner
        TestResult savedResult = testResultRepository.save(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedResult);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Test>> getTestsByCourse(@PathVariable Long courseId) {
        List<Test> tests = testRepository.findByCourseId(courseId);
        return ResponseEntity.ok(tests);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TestResult>> getTestResultsByUser(@PathVariable Long userId) {
        List<TestResult> results = testResultRepository.findByUserId(userId);
        return ResponseEntity.ok(results);
    }
    @GetMapping("/results/user/{userId}/course/{courseId}")
    public ResponseEntity<List<TestResult>> getResultsByUserAndCourse(
            @PathVariable Long userId,
            @PathVariable Long courseId
    ) {
        List<TestResult> results = testResultRepository.findByUserIdAndTestCourseId(userId, courseId);
        return ResponseEntity.ok(results);
    }
    public ResponseEntity<?> courseServiceFallback(
        TestResult result,
        String role,
        Throwable throwable
) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("Course service is currently unavailable. Please try again later.");
}

}
