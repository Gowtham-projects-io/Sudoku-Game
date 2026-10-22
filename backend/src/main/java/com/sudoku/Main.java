package com.sudoku;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Application entry point — wires all components together and starts the server.
 */
public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {

        // ── Composition: wire up the dependency graph ─────────────────────
        SudokuGameService service    = new SudokuGameService();
        SudokuController  controller = new SudokuController(service);

        // ── Create and configure the HTTP server ──────────────────────────
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/game/new",   controller.newGameHandler());
        server.createContext("/api/game/move",  controller.moveHandler());
        server.createContext("/api/game/hint",  controller.hintHandler());
        server.createContext("/api/game/check", controller.checkHandler());
        server.createContext("/api/game/reset", controller.resetHandler());
        server.createContext("/api/game/undo",  controller.undoHandler());

        // Static file handler for frontend (serves index.html, style.css, script.js)
        server.createContext("/", exchange -> {
            try (exchange) {
                String path = exchange.getRequestURI().getPath();
                if (path.startsWith("/api/")) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }
                if (path.equals("/") || path.isBlank()) {
                    path = "/index.html";
                }
                if (path.equals("/favicon.ico")) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                
                // Look for frontend directory in relative paths
                java.nio.file.Path filePath = java.nio.file.Path.of("frontend" + path);
                if (!java.nio.file.Files.exists(filePath)) {
                    filePath = java.nio.file.Path.of("../frontend" + path);
                }

                if (java.nio.file.Files.exists(filePath) && !java.nio.file.Files.isDirectory(filePath)) {
                    String contentType = "text/plain";
                    if (path.endsWith(".html")) contentType = "text/html; charset=utf-8";
                    else if (path.endsWith(".css")) contentType = "text/css; charset=utf-8";
                    else if (path.endsWith(".js")) contentType = "application/javascript; charset=utf-8";
                    else if (path.endsWith(".png")) contentType = "image/png";
                    else if (path.endsWith(".ico")) contentType = "image/x-icon";

                    byte[] bytes = java.nio.file.Files.readAllBytes(filePath);
                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (java.io.OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            }
        });

        // Thread pool so multiple requests can be handled concurrently
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║   Sudoku Game Server running on port " + PORT + "            ║");
        System.out.println("║   Web UI : http://localhost:" + PORT + "                      ║");
        System.out.println("║   API    : http://localhost:" + PORT + "/api/game             ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
    }
}
