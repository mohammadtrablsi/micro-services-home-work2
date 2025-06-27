// TestRepository.java
package com.example.assessment_service.repository;

import com.example.assessment_service.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByCourseId(Long courseId); // البحث عن الاختبارات الخاصة بدورة معينة
}
