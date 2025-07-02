// // Test.java
// package com.example.assessmentservice.entity;

// import jakarta.persistence.*;

// @Entity
// public class Test {
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;
    
//     private String title;
//     private String description;

//     private Long courseId;
    
//     // @ManyToOne
//     // @JoinColumn(name = "course_id")
//     // private Course course; // ربط الاختبار بالدورة

//     // Getters and Setters
// }
package com.example.assessmentservice.entity;

import jakarta.persistence.*;

@Entity
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Long courseId; // فقط حفظ معرف الدورة بدون العلاقة

    // === Getters and Setters ===

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
}
