package com.example.courseservice.repository;

import com.example.courseservice.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    
 
    List<Course> findByApprovedFalse();
    
   
    List<Course> findByTrainerId(Long trainerId);

 
    List<Course> findBySubscribedUserIdsContaining(Long userId); 
    List<Course> findByApprovedTrue();

}
