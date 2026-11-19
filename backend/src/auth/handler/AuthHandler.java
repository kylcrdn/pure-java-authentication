package auth.handler;

import auth.model.User;
import auth.service.AuthService;
import auth.session.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AuthHandler implements HttpHandler {
    private final AuthService authService;
    private final SessionManager sessionManager;
    private final Gson gson = new Gson();

    public AuthHandler(
            AuthService authService,
            SessionManager sessionManager
    ) {
        this.authService = authService;
        this.sessionManager = sessionManager;
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            exchange.getResponseHeaders().set(
                    "Cache-Control", "no-store"
            );

            exchange.getResponseHeaders().set(
                    "X-Content-Type-Options", "nosniff"
            );

            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (path.equals("/api/register") &&
                    method.equals("POST")) {

                handleRegister(exchange);
            } else if (path.equals("/api/login") && method.equals("POST")) {
                handleLogin(exchange);
            } else if (path.equals("/api/me") &&
                    method.equals("GET")) {

                handleMe(exchange);

            } else if (path.equals("/api/logout") &&
                    method.equals("POST")) {

                handleLogout(exchange);

            } else {
                sendJson(exchange, 404,
                        Map.of("message", "Not found"));
            }

        } catch (IllegalArgumentException e) {

            sendJson(exchange, 400,
                    Map.of("message", e.getMessage()));

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(exchange, 500,
                    Map.of("message", "Internal server error"));
        }
    }

    private void handleRegister(HttpExchange exchange)
            throws Exception {

        JsonObject body = readBody(exchange);

        String email = requiredString(body, "email");
        String password = requiredString(body, "password");

        authService.register(email, password);

        sendJson(exchange, 201,
                Map.of("message", "Registration successful"));
    }

    private void handleLogin(HttpExchange exchange)
            throws Exception {

        JsonObject body = readBody(exchange);

        String email = requiredString(body, "email");
        String password = requiredString(body, "password");

        User user = authService.login(email, password);
        if (user == null) {
            sendJson(exchange, 401,
                    Map.of("message", "Invalid email or password"));
            return;
        }

        String token = sessionManager.createSession(user);

        exchange.getResponseHeaders().add(
                "Set-Cookie",
                "SESSION=" + token +
                        "; HttpOnly; SameSite=Lax; Path=/; Max-Age=3600"
        );

        sendJson(exchange, 200,
                Map.of("message", "Login successful"));
    }

    private void handleMe(HttpExchange exchange)
            throws IOException {

        String token = getCookie(exchange, "SESSION");

        User user = sessionManager.getUser(token);

        if (user == null) {
            sendJson(exchange, 401,
                    Map.of("message", "Not logged in"));
            return;
        }

        sendJson(exchange, 200,
                Map.of(
                        "id", user.id(),
                        "email", user.email()
                ));
    }

    private void handleLogout(HttpExchange exchange)
            throws IOException {

        String token = getCookie(exchange, "SESSION");

        sessionManager.removeSession(token);

        exchange.getResponseHeaders().add(
                "Set-Cookie",
                "SESSION=; HttpOnly; SameSite=Lax; Path=/; Max-Age=0"
        );

        sendJson(exchange, 200,
                Map.of("message", "Logged out"));
    }

    private JsonObject readBody(HttpExchange exchange)
            throws IOException {

        String contentType = exchange.getRequestHeaders()
                .getFirst("Content-Type");

        if (contentType == null ||
                !contentType.startsWith("application/json")) {
            throw new IllegalArgumentException(
                    "Content-Type must be application/json"
            );
        }

        if (!exchange.getRequestHeaders()
                .getFirst("Content-Type")
                .startsWith("application/json")) {

            throw new IllegalArgumentException(
                    "Content-Type must be application/json"
            );
        }

        byte[] data = exchange.getRequestBody().readNBytes(8193);

        if (data.length > 8192) {
            throw new IllegalArgumentException("Request too large");
        }

        JsonObject body = gson.fromJson(
                new String(data, StandardCharsets.UTF_8),
                JsonObject.class
        );

        if (body == null) {
            throw new IllegalArgumentException("Invalid JSON");
        }

        return body;
    }

    private String requiredString(
            JsonObject body,
            String key
    ) {

        if (!body.has(key) || body.get(key).isJsonNull() ||
                !body.get(key).isJsonPrimitive() ||
                !body.get(key).getAsJsonPrimitive().isString()) {

            throw new IllegalArgumentException(
                    key + " is required"
            );
        }

        String value = body.get(key).getAsString();

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    key + " is required"
            );
        }

        return value;
    }

    private String getCookie(
            HttpExchange exchange,
            String name
    ) {

        String cookieHeader = exchange.getRequestHeaders()
                .getFirst("Cookie");

        if (cookieHeader == null) {
            return null;
        }

        for (String cookie : cookieHeader.split(";")) {

            String[] parts = cookie.trim().split("=", 2);

            if (parts.length == 2 &&
                    parts[0].equals(name)) {
                return parts[1];
            }
        }

        return null;
    }

    private void sendJson(
            HttpExchange exchange,
            int status,
            Object data
    ) throws IOException {

        byte[] response = gson.toJson(data)
                .getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(status, response.length);

        try (var output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
