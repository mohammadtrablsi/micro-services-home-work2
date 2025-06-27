// Test.java
package com.example.assessmentservice.entity;

import jakarta.persistence.*;

@Entity
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course; // ربط الاختبار بالدورة

    // Getters and Setters
}
