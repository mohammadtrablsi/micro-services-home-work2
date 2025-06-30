package com.example.userservice.service;

import com.example.userservice.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.entity.Role;
import com.example.userservice.entity.User;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.security.JwtUtil;
import com.example.userservice.security.SecurityConfig;
@Service
public class AuthService {
    @Autowired private UserRepository userRepo;
    @Autowired private RoleRepository roleRepo;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    public String registerLearner(RegisterRequest req) {
        Role learnerRole = roleRepo.findByName("LEARNER");
        User user = new User();
        user.setUsername(req.username);
        user.setEmail(req.email);
        user.setPassword(passwordEncoder.encode(req.password));
        user.setRole(learnerRole);
        userRepo.save(user);
        return jwtUtil.generateToken(user);
    }

    public String addTrainer(RegisterRequest req) {
        Role trainerRole = roleRepo.findByName("TRAINER");
        User user = new User();
        user.setUsername(req.username);
        user.setEmail(req.email);
        user.setPassword(passwordEncoder.encode(req.password));
        user.setRole(trainerRole);
        userRepo.save(user);
        return jwtUtil.generateToken(user);
    }

    public String addAdmin(RegisterRequest req) {
        Role adminRole = roleRepo.findByName("ADMIN");
        User user = new User();
        user.setUsername(req.username);
        user.setEmail(req.email);
        user.setPassword(passwordEncoder.encode(req.password));
        user.setRole(adminRole);
        userRepo.save(user);
        return jwtUtil.generateToken(user);
    }

    public String login(LoginRequest req) {
        User user = userRepo.findByUsername(req.username);
        if (user == null || !passwordEncoder.matches(req.password, user.getPassword()))
            throw new RuntimeException("Invalid credentials");

        return jwtUtil.generateToken(user);
    }
}
