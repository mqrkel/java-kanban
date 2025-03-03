package http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import exceptions.TaskOverlapException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            handleRequest(exchange);
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (TaskOverlapException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            sendError(exchange, "Internal Server Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected abstract void handleRequest(HttpExchange exchange) throws IOException;

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, getFormatted(message), 404);
    }

    protected void sendHasInteractions(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, getFormatted(message), 406);
    }

    protected void sendError(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, getFormatted(message), 500);
    }

    private static String getFormatted(String message) {
        return "{\"error\": \"%s\"}".formatted(message);
    }
}