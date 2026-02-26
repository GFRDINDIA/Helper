package com.helper.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JWT token generation/validation logic used by the gateway filter.
 */
class GatewayJwtTest {

    private static final String SECRET = "YOUR_JWT_SECRET_KEY_CHANGE_THIS_IN_PRODUCTION_MIN_256_BITS_LONG_ENOUGH";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private String generateToken(String userId, String role, long expiryMs) {
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .claim("email", "test@helper.app")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("Valid token: extracts claims correctly")
    void testValidToken() {
        String userId = UUID.randomUUID().toString();
        String token = generateToken(userId, "CUSTOMER", 60000);

        var claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();

        assertEquals(userId, claims.getSubject());
        assertEquals("CUSTOMER", claims.get("role", String.class));
        assertEquals("test@helper.app", claims.get("email", String.class));
    }

    @Test
    @DisplayName("Worker role token")
    void testWorkerToken() {
        String token = generateToken(UUID.randomUUID().toString(), "WORKER", 60000);
        var claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        assertEquals("WORKER", claims.get("role", String.class));
    }

    @Test
    @DisplayName("Admin role token")
    void testAdminToken() {
        String token = generateToken(UUID.randomUUID().toString(), "ADMIN", 60000);
        var claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        assertEquals("ADMIN", claims.get("role", String.class));
    }

    @Test
    @DisplayName("Expired token throws exception")
    void testExpiredToken() {
        String token = generateToken(UUID.randomUUID().toString(), "CUSTOMER", -1000);
        assertThrows(Exception.class, () ->
                Jwts.parser().verifyWith(key).build().parseSignedClaims(token));
    }

    @Test
    @DisplayName("Invalid signature throws exception")
    void testInvalidSignature() {
        SecretKey wrongKey = Keys.hmacShaKeyFor("WRONG_SECRET_KEY_THAT_IS_DEFINITELY_NOT_THE_RIGHT_ONE_256_BITS".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder().subject("test").signWith(wrongKey)
                .expiration(new Date(System.currentTimeMillis() + 60000)).compact();

        assertThrows(Exception.class, () ->
                Jwts.parser().verifyWith(key).build().parseSignedClaims(token));
    }

    @Test
    @DisplayName("Malformed token throws exception")
    void testMalformedToken() {
        assertThrows(Exception.class, () ->
                Jwts.parser().verifyWith(key).build().parseSignedClaims("not.a.valid.token"));
    }

    @Test
    @DisplayName("Token subject is UUID format")
    void testSubjectFormat() {
        UUID userId = UUID.randomUUID();
        String token = generateToken(userId.toString(), "CUSTOMER", 60000);
        var claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        assertDoesNotThrow(() -> UUID.fromString(claims.getSubject()));
    }
}
