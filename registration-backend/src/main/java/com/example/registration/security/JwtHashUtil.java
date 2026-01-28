package com.example.registration.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class JwtHashUtil {

    // Prevent instantiation
    private JwtHashUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : encoded) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        }catch (Exception e) {
            throw new IllegalStateException("Failed to hash JWT", e);
        }

    }
}
