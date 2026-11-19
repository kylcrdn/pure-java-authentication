package auth.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    // Local computer - standard port - database name
    private static final String URL =
            "jdbc:mysql://localhost:3306/java_login";

    // If you created a specific user for the database
    private static final String USER = "spriggan";

    private static final String PASSWORD = "Mamamo123";

    // Opens a connection to MySQL through Connector/J
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

}
