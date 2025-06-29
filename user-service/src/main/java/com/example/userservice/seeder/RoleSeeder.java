
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import com.example.userservice.repository.RoleRepository; // Import your RoleRepository class
import com.example.userservice.entity.Role; 
@Component
public class RoleSeeder implements CommandLineRunner {
    @Autowired private RoleRepository roleRepo;

    @Override
    public void run(String... args) {
        if (roleRepo.count() == 0) {
            roleRepo.save(new Role(null, "ADMIN", null));
            roleRepo.save(new Role(null, "TRAINER", null));
            roleRepo.save(new Role(null, "LEARNER", null));
        }
    }
}
