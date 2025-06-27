package com.example.courseservice.repository;

import com.example.courseservice.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    
    // جلب الدورات التي لم تتم الموافقة عليها بعد
    List<Course> findByApprovedFalse();
    
    // جلب الدورات بناءً على Trainer ID
    List<Course> findByTrainerId(Long trainerId);
}
