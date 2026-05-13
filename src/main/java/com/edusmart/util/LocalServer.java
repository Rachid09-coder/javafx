package com.edusmart.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class LocalServer {

    private static HttpServer server;
    private static final int DEFAULT_PORT = 8085;
    public static String LOCAL_IP = "localhost";

    /**
     * Starts a simple file-serving HTTP server for generated PDFs.
     * If the default port is unavailable, falls back to an ephemeral port.
     */
    public static void start() {
        try {
            LOCAL_IP = InetAddress.getLocalHost().getHostAddress();
            int configuredPort = DEFAULT_PORT;
            String portProp = System.getProperty("local.server.port");
            if (portProp != null && !portProp.isBlank()) {
                try {
                    configuredPort = Integer.parseInt(portProp.trim());
                } catch (NumberFormatException nfe) {
                    System.err.println("Invalid local.server.port value: " + portProp + ", using default " + DEFAULT_PORT);
                    configuredPort = DEFAULT_PORT;
                }
            }

            try {
                server = HttpServer.create(new InetSocketAddress(configuredPort), 0);
            } catch (IOException bindEx) {
                // port in use or unavailable -> fallback to an ephemeral port
                System.err.println("Port " + configuredPort + " unavailable, falling back to an ephemeral port: " + bindEx.getMessage());
                server = HttpServer.create(new InetSocketAddress(0), 0);
            }

            final HttpServer finalServer = server;
            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String path = exchange.getRequestURI().getPath();
                    if (path.startsWith("/")) {
                        path = path.substring(1);
                    }

                    File file = new File("generated_pdfs", path);
                    if (file.exists() && !file.isDirectory()) {
                        byte[] bytes = Files.readAllBytes(file.toPath());
                        exchange.getResponseHeaders().add("Content-Type", "application/pdf");
                        exchange.sendResponseHeaders(200, bytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(bytes);
                        }
                    } else {
                        String response = "404 (Not Found)\n";
                        exchange.sendResponseHeaders(404, response.length());
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                    }
                }
            });

            server.setExecutor(null); // creates a default executor
            server.start();
            int port = finalServer.getAddress().getPort();
            System.out.println("Local PDF server started on http://" + LOCAL_IP + ":" + port);
        } catch (Exception e) {
            System.err.println("Failed to start local server: " + e.getMessage());
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Local PDF server stopped.");
        }
    }
}
