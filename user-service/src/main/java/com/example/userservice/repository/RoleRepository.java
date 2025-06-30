package com.example.userservice.repository;

import com.example.userservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<Role, Long> {
    // Optional: to find roles by name
    @Query("SELECT r FROM Role r WHERE r.name = ?1")
    Role findByName(String name);
}
