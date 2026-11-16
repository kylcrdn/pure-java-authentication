package auth.server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    private HttpServer server;

    public void start() {
        try {
            // Creating the server
            server = HttpServer.create(
                    new InetSocketAddress(8080),
                    0
            );

            System.out.println("Server running on port 8080");

            server.start();
        } catch (IOException e) {
            System.out.println("Could not start server: " + e.getMessage());
        }
    }
}
