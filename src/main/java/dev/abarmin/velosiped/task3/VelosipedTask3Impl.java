package dev.abarmin.velosiped.task3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import dev.abarmin.velosiped.task2.Request;
import dev.abarmin.velosiped.task2.Response;
import dev.abarmin.velosiped.task2.VelosipedTask2;

public class VelosipedTask3Impl implements VelosipedTask3 {

    private static VelosipedJsonAdapter jsonAdapter = new VelosipedJsonAdapterImpl();

    private static final HttpHandler sumEndpoint = t -> {
        String json = new BufferedReader(
                new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        Request request = jsonAdapter.parse(json, Request.class);
        Response response = new Response(request.getArg1() + request.getArg2());
        byte[] responseBytes = jsonAdapter.writeAsJson(response).getBytes();
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
