// // // TestResult.java
// // package com.example.assessmentservice.entity;

// // import jakarta.persistence.*;

// // @Entity
// // public class TestResult {
// //     @Id
// //     @GeneratedValue(strategy = GenerationType.IDENTITY)
// //     private Long id;
    
// //     @ManyToOne
// //     @JoinColumn(name = "user_id")
// //     private User user; // ربط المتعلم بالاختبار
    
// //     @ManyToOne
// //     @JoinColumn(name = "test_id")
// //     private Test test; // ربط الاختبار بالمشترك
    
// //     private Boolean passed; // هل اجتاز المشترك الاختبار؟
// //     private Integer score; // درجة المشترك

// //     // Getters and Setters
// // }
// package com.example.assessmentservice.entity;

// import jakarta.persistence.*;

// @Entity
// public class TestResult {
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     private Long userId; // 👈 فقط نحتفظ بالـ user_id

//     @ManyToOne
//     @JoinColumn(name = "test_id")
//     private Test test;

//     private Boolean passed;
//     private Integer score;

//     // Getters and Setters
// }
package com.example.assessmentservice.entity;

import jakarta.persistence.*;

@Entity
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // فقط نحتفظ بـ user_id

    @ManyToOne
    @JoinColumn(name = "test_id")
    private Test test;

    private Boolean passed;
    private Integer score;

    // === Getters and Setters ===

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}