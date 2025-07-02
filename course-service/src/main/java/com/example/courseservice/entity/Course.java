package com.example.courseservice.entity;

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
    private Double price;

    @ElementCollection
    @CollectionTable(
        name = "course_subscriptions",
        joinColumns = @JoinColumn(name = "course_id")
    )
    @Column(name = "user_id") // اسم العمود الذي يخزن الـ Long
    private List<Long> subscribedUserIds = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public List<Long> getSubscribedUserIds() { return subscribedUserIds; }
    public void setSubscribedUserIds(List<Long> subscribedUserIds) { this.subscribedUserIds = subscribedUserIds; }
}
