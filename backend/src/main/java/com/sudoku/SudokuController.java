package com.sudoku;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * HTTP request handler — demonstrates COMPOSITION.
 *
 * OOP Concept: Composition
 *   SudokuController IS-NOT-A service; it HAS-A SudokuGameService reference
 *   and delegates all business logic to it.
 *
 * Each context path is handled by a dedicated private static inner class
 * that implements HttpHandler, keeping concerns separated.
 */
public class SudokuController {

    // ── Composed service (Composition) ───────────────────────────────────────
    private final SudokuGameService service;

    public SudokuController(SudokuGameService service) {
        this.service = service;
    }

    // ── Handler factories ────────────────────────────────────────────────────

    public HttpHandler newGameHandler() {
        return exchange -> {
            if (!handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }
            try {
                JSONObject body = parseBody(exchange);
                String difficulty = body.optString("difficulty", "medium");
                JSONObject result = service.newGame(difficulty);
                sendJson(exchange, 200, result);
            } catch (IllegalArgumentException | org.json.JSONException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (IOException | RuntimeException e) {
                sendError(exchange, 500, e.getMessage());
            }
        };
    }

    public HttpHandler moveHandler() {
        return exchange -> {
            if (!handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }
            try {
                JSONObject body   = parseBody(exchange);
                String gameId     = body.getString("gameId");
                int row           = body.getInt("row");
                int col           = body.getInt("col");
                int value         = body.getInt("value");
                JSONObject result = service.validateMove(gameId, row, col, value);
                sendJson(exchange, 200, result);
            } catch (IllegalArgumentException | IllegalStateException | org.json.JSONException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (IOException | RuntimeException e) {
                sendError(exchange, 500, e.getMessage());
            }
        };
    }

    public HttpHandler hintHandler() {
        return exchange -> {
            if (!handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }
            try {
                JSONObject body   = parseBody(exchange);
                String gameId     = body.getString("gameId");
                JSONObject result = service.getHint(gameId);
                sendJson(exchange, 200, result);
            } catch (IllegalArgumentException | IllegalStateException | org.json.JSONException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (IOException | RuntimeException e) {
                sendError(exchange, 500, e.getMessage());
            }
        };
    }

    public HttpHandler checkHandler() {
        return exchange -> {
            if (!handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }
            try {
                JSONObject body   = parseBody(exchange);
                String gameId     = body.getString("gameId");
                JSONObject result = service.checkCompletion(gameId);
                sendJson(exchange, 200, result);
            } catch (IllegalArgumentException | IllegalStateException | org.json.JSONException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (IOException | RuntimeException e) {
                sendError(exchange, 500, e.getMessage());
            }
        };
    }

    public HttpHandler resetHandler() {
        return exchange -> {
            if (!handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }
            try {
                JSONObject body   = parseBody(exchange);
                String gameId     = body.getString("gameId");
                JSONObject result = service.resetGame(gameId);
                sendJson(exchange, 200, result);
            } catch (IllegalArgumentException | IllegalStateException | org.json.JSONException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (IOException | RuntimeException e) {
                sendError(exchange, 500, e.getMessage());
            }
        };
    }

    public HttpHandler undoHandler() {
        return exchange -> {
            if (!handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }
            try {
                JSONObject body   = parseBody(exchange);
                String gameId     = body.getString("gameId");
                JSONObject result = service.undoMove(gameId);
                sendJson(exchange, 200, result);
            } catch (IllegalArgumentException | IllegalStateException | org.json.JSONException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (IOException | RuntimeException e) {
                sendError(exchange, 500, e.getMessage());
            }
        };
    }

    // ── HTTP utilities ───────────────────────────────────────────────────────

    /**
     * Handles CORS pre-flight and adds CORS headers to every response.
     * @return false if the exchange was already answered (OPTIONS pre-flight).
     */
    private boolean handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin",  "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            try (exchange) {
                exchange.sendResponseHeaders(204, -1);
            }
            return false;
        }
        return true;
    }

    private JSONObject parseBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return body.isBlank() ? new JSONObject() : new JSONObject(body);
        }
    }

    private void sendJson(HttpExchange exchange, int status, JSONObject json) throws IOException {
        byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        JSONObject err = new JSONObject();
        err.put("error", message);
        sendJson(exchange, status, err);
    }
}
