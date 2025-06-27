package com.example.userservice.repository;

import com.example.userservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    // Optional: to find roles by name
    Role findByName(String name);
}
