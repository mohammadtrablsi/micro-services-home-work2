// TestResult.java
package com.example.assessmentservice.entity;

import jakarta.persistence.*;

@Entity
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // ربط المتعلم بالاختبار
    
    @ManyToOne
    @JoinColumn(name = "test_id")
    private Test test; // ربط الاختبار بالمشترك
    
    private Boolean passed; // هل اجتاز المشترك الاختبار؟
    private Integer score; // درجة المشترك

    // Getters and Setters
}
