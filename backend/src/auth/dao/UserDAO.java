package auth.dao;

import auth.database.Database;
import auth.model.User;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// DAO or Data Access Object. Communicates with the database.
public class UserDAO {

    // Checks if email already exists
    public boolean emailExists(String email) throws SQLException {

        String sql = "SELECT id FROM users WHERE email = ?";

        try(
                Connection connection = Database.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
        ) {
            statement.setString(1, email);
            try(ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    // Creating a new User
    public void createUser(
            String email,
            String passwordHash
    ) throws SQLException {

        String sql = """
                INSERT INTO users (email, password_hash)
                VALUES (?, ?)
                """;

        try(
                Connection connection = Database.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ) {
            statement.setString(1,email);
            statement.setString(2,passwordHash);

            statement.executeUpdate();
        }
    }

    public String findPasswordHash(String email) throws SQLException {

        String sql = """
                SELECT password_hash
                FROM users
                WHERE email = ?
                """;

        try(
                Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ) {
            statement.setString(1, email);

            try(ResultSet result = statement.executeQuery()){
                if (result.next()) {
                    return result.getString("password_hash");
                }

                return null;
            }
        }
    }

    public User findByEmail(String email) throws SQLException {

        String sql = """
                SELECT id, email
                FROM users
                WHERE email = ?
                """;

        try (
                Connection connection = Database.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return new User(
                            result.getLong("id"),
                            result.getString("email")
                    );
                }

                return null;
            }
        }
    }
}
