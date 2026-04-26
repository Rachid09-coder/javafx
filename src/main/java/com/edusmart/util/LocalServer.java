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
    private static final int PORT = 8085;
    public static String LOCAL_IP = "localhost";

    public static void start() {
        try {
            LOCAL_IP = InetAddress.getLocalHost().getHostAddress();
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            
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
                        OutputStream os = exchange.getResponseBody();
                        os.write(bytes);
                        os.close();
                    } else {
                        String response = "404 (Not Found)\n";
                        exchange.sendResponseHeaders(404, response.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                }
            });
            
            server.setExecutor(null); // creates a default executor
            server.start();
            System.out.println("Local PDF server started on http://" + LOCAL_IP + ":" + PORT);
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
