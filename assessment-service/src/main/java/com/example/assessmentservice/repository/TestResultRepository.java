// TestResultRepository.java
package com.example.assessmentservice.repository;

import com.example.assessmentservice.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByUserId(Long userId); // البحث عن نتائج اختبارات متعلم معين
    List<TestResult> findByUserIdAndTestCourseId(Long userId, Long courseId);
}
