package turuq.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import turuq.backend.entities.User;
import turuq.backend.repositories.UserRepository;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.email}")
    private String seedEmail;

    @Value("${app.seed.password}")
    private String seedPassword;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(seedEmail)) {
            log.info("Seed user '{}' already exists, skipping seeding", seedEmail);
            return;
        }

        User seeded = User.builder()
                .email(seedEmail)
                .password(passwordEncoder.encode(seedPassword))
                .build();

        userRepository.save(seeded);
        log.info("Seeded default login user '{}'", seedEmail);
    }
}
