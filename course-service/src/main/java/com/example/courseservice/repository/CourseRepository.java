package com.example.courseservice.repository;

import com.example.courseservice.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    
    // جلب الدورات التي لم تتم الموافقة عليها بعد
    List<Course> findByApprovedFalse();
    
    // جلب الدورات بناءً على Trainer ID
    List<Course> findByTrainerId(Long trainerId);

        // ✅ Add this:
     // ✅ هذه هي الدالة المطلوبة
    List<Course> findBySubscribedUserIdsContaining(Long userId); // Only works if you used @ElementCollection
    List<Course> findByApprovedTrue();

}
