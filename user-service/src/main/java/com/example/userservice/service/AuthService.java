import com.example.user_service.repository.UserRepository;
import com.example.user_service.repository.RoleRepository;
import com.example.user_service.security.JwtUtil;
import com.example.user_service.security.SecurityConfig;
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
