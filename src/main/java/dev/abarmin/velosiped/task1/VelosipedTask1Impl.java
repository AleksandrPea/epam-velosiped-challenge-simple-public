package dev.abarmin.velosiped.task1;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class VelosipedTask1Impl implements VelosipedTask1 {

    private static final HttpHandler sumEndpoint = t -> {
        String query = t.getRequestURI().getQuery();
        String[] params = query.split("&");
        int a = Integer.parseInt(params[0].split("=")[1]);
        int b = Integer.parseInt(params[1].split("=")[1]);
        String response = "" + (a + b);
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    };

    private HttpServer server;

    @Override
    public synchronized void startServer(int port) {
        if (server != null) {
            throw new RuntimeException("Please, stop the server");
        }
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/sum", sumEndpoint);
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    @Override
    public synchronized void stopServer() {
        if (server == null) {
            throw new RuntimeException("Please, start the server");
        }
        server.stop(1);
        server = null;
    }
}
