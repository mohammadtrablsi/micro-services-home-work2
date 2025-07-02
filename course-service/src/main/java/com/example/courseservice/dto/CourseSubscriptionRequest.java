package com.example.courseservice.dto;

public class CourseSubscriptionRequest {
    private Long courseId;

    // Constructors
    public CourseSubscriptionRequest() {}

    public CourseSubscriptionRequest(Long courseId) {
        this.courseId = courseId;
    }

    // Getter and Setter
    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
}
