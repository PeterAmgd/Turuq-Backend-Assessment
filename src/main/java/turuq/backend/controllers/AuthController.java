package turuq.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import turuq.backend.entities.User;
import turuq.backend.exception.InvalidCredentialsException;
import turuq.backend.payloads.requests.AuthRequest;
import turuq.backend.payloads.responses.AuthResponse;
import turuq.backend.repositories.UserRepository;
import turuq.backend.utils.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Obtain a JWT to call the protected /users endpoints")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public AuthController(
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
       this.passwordEncoder = passwordEncoder;
       this.userRepository = userRepository;
    }

    @Operation(
            summary = "Log in and receive a JWT",
            description = "Default seeded account: username 'user', password 'user123'."
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        AuthResponse response = AuthResponse.builder()
                .accessToken(token)
                .expiresInMs(jwtUtil.getExpirationMs())
                .build();
        return ResponseEntity.ok(response);
    }
}
