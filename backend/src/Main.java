import auth.dao.UserDAO;
import auth.database.Database;
import auth.handler.AuthHandler;
import auth.service.AuthService;
import auth.session.SessionManager;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        try (Connection connection = Database.getConnection()) {
            System.out.println("MySQL connected successfully!");
        }

        UserDAO userDAO = new UserDAO();
        AuthService authService = new AuthService(userDAO);
        SessionManager sessionManager = new SessionManager();

        HttpServer server = HttpServer.create(
                new InetSocketAddress("localhost", 8080),
                0
        );

        server.createContext(
                "/api/",
                new AuthHandler(authService, sessionManager)
        );

        Path frontendRoot = Path.of("..", "frontend")
                .toAbsolutePath()
                .normalize();
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String relativePath = path.equals("/")
                    ? "index.html"
                    : path.substring(1);
            Path file = frontendRoot.resolve(relativePath).normalize();

            if (!file.startsWith(frontendRoot)) {
                exchange.sendResponseHeaders(403, -1);
                exchange.close();
                return;
            }

            if (!Files.isRegularFile(file)) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }

            String contentType = "text/plain; charset=UTF-8";
            if (path.endsWith(".html")) {
                contentType = "text/html; charset=UTF-8";
            } else if (path.endsWith(".css")) {
                contentType = "text/css; charset=UTF-8";
            } else if (path.endsWith(".js")) {
                contentType = "application/javascript; charset=UTF-8";
            }

            byte[] content = Files.readAllBytes(file);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, content.length);

            try (var output = exchange.getResponseBody()) {
                output.write(content);
            }
        });

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("Java backend is running!");
        System.out.println("Open http://localhost:8080");
    }
}
