package auth.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = requireEnv("DB_URL");
    private static final String USER = requireEnv("DB_USER");
    private static final String PASSWORD = requireEnv("DB_PASSWORD");

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Missing required environment variable: " + name
            );
        }
        return value;

    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try(Connection connection = getConnection()) {
            System.out.println(connection.getCatalog() + " was successfully connected");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}