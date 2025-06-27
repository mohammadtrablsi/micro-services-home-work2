package com.example.course_service.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Long trainerId;
    private boolean approved;

    @ManyToMany
    @JoinTable(
        name = "user_course_subscription", // The intermediate table
        joinColumns = @JoinColumn(name = "course_id"),  // The foreign key to Course
        inverseJoinColumns = @JoinColumn(name = "user_id")  // The foreign key to User
    )
    private List<User> subscribedUsers = new ArrayList<>();  // List of users who subscribed to this course

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public List<User> getSubscribedUsers() { return subscribedUsers; }
    public void setSubscribedUsers(List<User> subscribedUsers) { this.subscribedUsers = subscribedUsers; }
}
