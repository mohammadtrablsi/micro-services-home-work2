// package com.example.userservice.controller;

// import com.example.userservice.entity.Item;
// import com.example.userservice.repository.ItemRepository;
// import org.springframework.web.bind.annotation.*;

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
    public AuthResponse registerLearner(@RequestBody RegisterRequest req) {
        return new AuthResponse(authService.registerLearner(req));
    }

    @PostMapping("/register/trainer")
    public AuthResponse registerTrainer(@RequestBody RegisterRequest req) {
        return new AuthResponse(authService.addTrainer(req));
    }

    @PostMapping("/register/admin")
    public AuthResponse registerAdmin(@RequestBody RegisterRequest req) {
        return new AuthResponse(authService.addAdmin(req));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        return new AuthResponse(authService.login(req));
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

}
