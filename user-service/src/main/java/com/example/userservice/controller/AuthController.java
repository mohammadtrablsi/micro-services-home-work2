package com.example.userservice.controller;

import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.AuthService;
import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.dto.UserDTO;
import com.example.userservice.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;   
// import java.util.List;

// @RestController
// @RequestMapping("/users")
// public class ItemController {
//     private final ItemRepository repo;
//     public ItemController(ItemRepository repo) { this.repo = repo; }

//     @PostMapping
//     public Item create(@RequestBody Item item) { return repo.save(item); }

//     @GetMapping
//     public List<Item> getAll() { return repo.findAll(); }

//     @GetMapping("/<built-in function id>")
//     public Item getOne(@PathVariable Long id) { return repo.findById(id).orElseThrow(); }
// }
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;

   @PostMapping("/register/learner")
public ResponseEntity<AuthResponse> registerLearner(@RequestBody RegisterRequest req) {
    try {
        String token = authService.registerLearner(req);
        return ResponseEntity.ok(new AuthResponse(token));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new AuthResponse("Error: " + e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(new AuthResponse("Unexpected error: " + e.getMessage()));
    }
}

@PostMapping("/register/trainer")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<AuthResponse> registerTrainer(@RequestBody RegisterRequest req) {
    try {
        String token = authService.addTrainer(req);
        return ResponseEntity.ok(new AuthResponse(token));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new AuthResponse("Error: " + e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(new AuthResponse("Unexpected error: " + e.getMessage()));
    }
}



@PostMapping("/register/admin")
public ResponseEntity<AuthResponse> registerAdmin(@RequestBody RegisterRequest req) {
    try {
        String token = authService.addAdmin(req);
        return ResponseEntity.ok(new AuthResponse(token));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new AuthResponse("Error: " + e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(new AuthResponse("Unexpected error: " + e.getMessage()));
    }
}


@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
    try {
        String token = authService.login(req);
        return ResponseEntity.ok(new AuthResponse(token));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new AuthResponse("Error: " + e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(new AuthResponse("Unexpected error: " + e.getMessage()));
    }
}


    @GetMapping("/users/name/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
    User user = userRepository.findByUsername(username);
    if (user == null) {
        return ResponseEntity.notFound().build();
    }

    UserDTO dto = new UserDTO(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getRole().getName()
    );
    return ResponseEntity.ok(dto);
}

@GetMapping("/users/{id}/balance")
public ResponseEntity<Double> getUserBalance(@PathVariable Long id) {
    User user = userRepository.findById(id).orElse(null);
    if (user == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    return ResponseEntity.ok(user.getWalletBalance());
}



    @PostMapping("/users/{id}/deduct")
    public ResponseEntity<?> deductBalance(
            @PathVariable Long id,
            @RequestParam Double amount) {

        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest().body("Amount must be positive");
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (user.getWalletBalance() < amount) {
            return ResponseEntity.status(402).body("Insufficient funds");
        }

        user.setWalletBalance(user.getWalletBalance() - amount);
        userRepository.save(user);

        return ResponseEntity.ok("Balance updated");
    }

}
