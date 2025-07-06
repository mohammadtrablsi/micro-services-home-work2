package com.example.paymentservice.entity;

import jakarta.persistence.*;

@Entity
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long learnerId;
    private Long courseId;
    private Double amount;

    public PaymentTransaction() {
    }

    public PaymentTransaction(Long learnerId, Long courseId, Double amount) {
        this.learnerId = learnerId;
        this.courseId = courseId;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLearnerId() { return learnerId; }
    public void setLearnerId(Long learnerId) { this.learnerId = learnerId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
