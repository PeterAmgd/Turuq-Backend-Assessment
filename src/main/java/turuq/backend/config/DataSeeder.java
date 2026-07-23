//package turuq.backend.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import turuq.backend.entities.User;
//import turuq.backend.repositories.UserRepository;
//
//import java.time.Instant;
//
//@Component
//@RequiredArgsConstructor
//public class DataSeeder implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//
//    @Override
//    public void run(String... args) {
//
//        String email = "user@example.com";
//
//        if (userRepository.findByEmail(email).isEmpty()) {
//
//            User user = User.builder()
//                    .name("user")
//                    .email(email)
//                    .password(passwordEncoder.encode("user123"))
//                    .age(0)
//                    .createdAt(Instant.now())
//                    .build();
//
//            userRepository.save(user);
//
//            System.out.println("Default user created: user / user123");
//        }
//        else {
//            System.out.println("Default user already exists");
//        }
//    }
//}
