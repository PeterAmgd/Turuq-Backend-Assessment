package turuq.backend.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import turuq.backend.exception.InvalidCredentialsException;
import turuq.backend.payloads.requests.AuthRequest;
import turuq.backend.payloads.responses.AuthResponse;
import turuq.backend.utils.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final String demoUsername;
    private final String demoPassword;

    public AuthController(
            JwtUtil jwtUtil,
            @Value("${app.auth.demo-username}") String demoUsername,
            @Value("${app.auth.demo-password}") String demoPassword) {
        this.jwtUtil = jwtUtil;
        this.demoUsername = demoUsername;
        this.demoPassword = demoPassword;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        if (!demoUsername.equals(request.getEmail()) || !demoPassword.equals(request.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(request.getEmail());
        AuthResponse response = AuthResponse.builder()
                .accessToken(token)
                .expiresInMs(jwtUtil.getExpirationMs())
                .build();
        return ResponseEntity.ok(response);
    }
}
