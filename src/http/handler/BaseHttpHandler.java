package http.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import exceptions.TaskOverlapException;
import http.handler.adapter.DurationAdapter;
import http.handler.adapter.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class BaseHttpHandler implements HttpHandler {

    protected final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    protected void sendJson(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    processGet(exchange, path);
                    break;
                case "POST":
                    processPost(exchange, path);
                    break;
                case "DELETE":
                    processDelete(exchange, path);
                    break;
                case "PUT":
                    processPut(exchange, path);
                    break;
                default:
                    sendError(exchange, "Method Not Allowed", 405);
                    break;
            }
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }

    protected void processGet(HttpExchange exchange, String path) throws IOException {
        sendError(exchange, "GET method not supported,", 405);
    }

    protected void processPost(HttpExchange exchange, String path) throws IOException {
        sendError(exchange, "POST method not supported,", 405);
    }

    protected void processDelete(HttpExchange exchange, String path) throws IOException {
        sendError(exchange, "DELETE method not supported,", 405);
    }

    protected void processPut(HttpExchange exchange, String path) throws IOException {
        sendError(exchange, "PUT method not supported,", 405);
    }

    private void handleException(HttpExchange exchange, Exception e) throws IOException {
        int statusCode;
        String message;

        if (e instanceof NotFoundException) {
            statusCode = 404;
            message = e.getMessage();
        } else if (e instanceof TaskOverlapException) {
            statusCode = 406;
            message = e.getMessage();
        } else {
            statusCode = 500;
            message = "Internal Server Error: " + e.getMessage();
            e.printStackTrace();
        }

        sendJson(exchange, getFormatted(message), statusCode);
    }

    protected void sendError(HttpExchange exchange, String message, int statusCode) throws IOException {
        sendJson(exchange, getFormatted(message), statusCode);
    }

    private static String getFormatted(String message) {
        return "{\"error\": \"%s\"}".formatted(message);
    }

    public Gson gson() {
        return gson;
    }
}