package turuq.backend;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import turuq.backend.utils.JwtUtil;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pure unit tests for the JWT signing/verification logic in isolation, with no Spring
 * context and no database - fast feedback on the security-critical token code path.
 */
class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil(
            "unit-test-secret-key-must-be-at-least-256-bits-long-for-hs256",
            3_600_000L // 1 hour
    );

    @Test
    void generateToken_thenValidate_returnsOriginalSubject() {
        String token = jwtUtil.generateToken("user");

        assertNotNull(token);
        assertEquals("user", jwtUtil.validateAndGetSubject(token));
    }

    @Test
    void validateAndGetSubject_throwsJwtException_forGarbageToken() {
        assertThrows(JwtException.class, () -> jwtUtil.validateAndGetSubject("not-a-real-jwt"));
    }

    @Test
    void validateAndGetSubject_throwsJwtException_whenSignedWithDifferentSecret() {
        JwtUtil otherSigner = new JwtUtil(
                "a-completely-different-secret-key-also-256-bits-plus-long",
                3_600_000L
        );
        String tokenFromOtherSigner = otherSigner.generateToken("user");

        assertThrows(JwtException.class, () -> jwtUtil.validateAndGetSubject(tokenFromOtherSigner));
    }
}
