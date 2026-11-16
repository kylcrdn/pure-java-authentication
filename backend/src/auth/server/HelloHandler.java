package auth.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class HelloHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Making the handler to only allow GET
        if (!exchange.getRequestMethod().equals("GET")){
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        String response = "Hello from Java backend!";



        byte[] responseBytes = response.getBytes(
                java.nio.charset.StandardCharsets.UTF_8
        );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/plain; charset=UTF-8"
        );

        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(responseBytes);
        }
    }
}
