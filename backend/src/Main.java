import auth.dao.UserDAO;
import auth.database.Database;
import auth.handler.AuthHandler;
import auth.service.AuthService;
import auth.session.SessionManager;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
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

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("Java backend is running!");
        System.out.println("Backend API is listening on http://localhost:8080");
    }
}
