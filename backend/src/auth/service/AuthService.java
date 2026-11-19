package auth.service;

import auth.dao.UserDAO;
import auth.model.User;
import auth.security.PasswordUtil;

import java.sql.SQLException;
import java.util.Locale;

// Service contains the business logic
public class AuthService {
    private final UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // Register a User
    public void register(String email, String password) throws Exception {
        email = normalizeEmail(email);

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Please enter a valid email address."
            );
        }

        if (password == null || password.length() < 12) {
            throw new IllegalArgumentException(
                    "Password must be at least 12 characters."
            );
        }

        String passwordHash = PasswordUtil.hashPassword(password);
        try {
            userDAO.createUser(email, passwordHash);
        } catch (SQLException exception) {
            // MySQL duplicate-key error
            if ("23000".equals(exception.getSQLState())) {
                throw new IllegalArgumentException(
                        "An account with this email already exists."
                );
            }

            throw exception;
        }
    }

    // Logging in an existing User
    public User login(String email, String password)
            throws Exception {

        email = normalizeEmail(email);

        if (password == null || password.isEmpty()) {
            return null;
        }

        String storedHash =
                userDAO.findPasswordHash(email);

        if (storedHash == null) {
            return null;
        }

        boolean valid = PasswordUtil.verifyPassword(
                password,
                storedHash
        );

        if (!valid) {
            return null;
        }

        return userDAO.findByEmail(email);
    }

    // For validating email
    private String normalizeEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException(
                    "Email is required."
            );
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}

