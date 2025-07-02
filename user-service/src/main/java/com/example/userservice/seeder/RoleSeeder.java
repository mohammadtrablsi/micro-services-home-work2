// package com.example.userservice.seeder;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;
// import org.springframework.boot.CommandLineRunner;
// import com.example.userservice.repository.RoleRepository; // Import your RoleRepository class
// import com.example.userservice.entity.Role;

// @Component
// public class RoleSeeder implements CommandLineRunner {
//     @Autowired
//     private RoleRepository roleRepo;

//     @Override
//     public void run(String... args) {
//         Role admin = new Role();
//         admin.setName("ADMIN");

//         Role trainer = new Role();
//         trainer.setName("TRAINER");

//         Role learner = new Role();
//         learner.setName("LEARNER");

//         roleRepo.save(admin);
//         roleRepo.save(trainer);
//         roleRepo.save(learner);

//     }
// }
