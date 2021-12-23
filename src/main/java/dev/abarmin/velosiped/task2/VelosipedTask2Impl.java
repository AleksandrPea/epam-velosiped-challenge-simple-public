package dev.abarmin.velosiped.task2;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class VelosipedTask2Impl implements VelosipedTask2 {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final HttpHandler sumEndpoint = t -> {
        Request request = objectMapper.readValue(t.getRequestBody(), Request.class);
        Response response = new Response(request.getArg1() + request.getArg2());
        byte[] responseBytes = objectMapper.writeValueAsBytes(response);
        t.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = t.getResponseBody();
        os.write(responseBytes);
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
