package com.example.user_service.repository;

import com.example.user_service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    // Optional: to find roles by name
    Role findByName(String name);
}
