package auth.session;

import auth.model.User;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

// Remembering which user is logged in
// and generating a random session token and store it in memory
public class SessionManager {

    private final ConcurrentHashMap<String, Session> sessions =
            new ConcurrentHashMap<>();

    private final SecureRandom random = new SecureRandom();

    private static final long SESSION_DURATION =
            60 * 60 * 1000; // 1 hour

    public String createSession(User user) {
        Objects.requireNonNull(user, "user");

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        long expiresAt = System.currentTimeMillis()
                + SESSION_DURATION;

        sessions.put(token, new Session(user, expiresAt));

        return token;
    }

    public User getUser(String token) {

        if (token == null) {
            return null;
        }

        Session session = sessions.get(token);

        if (session == null) {
            return null;
        }

        if (System.currentTimeMillis() > session.expiresAt()) {
            sessions.remove(token);
            return null;
        }

        return session.user();
    }

    public void removeSession(String token) {

        if (token != null) {
            sessions.remove(token);
        }
    }

    // Session record
    private record Session(
            User user,
            long expiresAt
    ) {
    }
}
