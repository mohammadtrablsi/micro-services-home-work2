package com.example.userservice.entity;

import jakarta.persistence.*;
import java.util.List;
@Entity
@Table(name = "\"user\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String email;

    private String password;

    private Double walletBalance;
    
    // @ManyToMany(mappedBy = "subscribedUsers")  // الربط مع الكائن الآخر (دورة)
    // private List<CourseDTO> subscribedCourses;  // قائمة الدورات التي اشترك فيها المستخدم


    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(Double walletBalance) { this.walletBalance = walletBalance; }
}
