package auth.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

// Hashing the password of the Users when saved inside MySQL
// Using Java's built-in PBKDF2WithHmacSHA256 algorithm where it generates a salted password hash

public class PasswordUtil {

    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    public static String hashPassword(String password)
            throws Exception {

        byte[] salt = new byte[SALT_LENGTH];

        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        byte[] hash = generateHash(password, salt, ITERATIONS);

        return ITERATIONS + ":" +
                Base64.getEncoder().encodeToString(salt) + ":" +
                Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(
            String password,
            String storedHash
    ) throws Exception {

        String[] parts = storedHash.split(":");

        int iterations = Integer.parseInt(parts[0]);

        byte[] salt =
                Base64.getDecoder().decode(parts[1]);

        byte[] expectedHash =
                Base64.getDecoder().decode(parts[2]);

        byte[] actualHash =
                generateHash(password, salt, iterations);

        return MessageDigest.isEqual(
                expectedHash,
                actualHash
        );
    }

    private static byte[] generateHash(
            String password,
            byte[] salt,
            int iterations
    ) throws Exception {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                KEY_LENGTH
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            return factory.generateSecret(spec).getEncoded();

        } finally {
            spec.clearPassword();
        }
    }
}

