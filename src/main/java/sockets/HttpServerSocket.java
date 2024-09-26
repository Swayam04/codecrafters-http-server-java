package sockets;

import http.HttpRequest;
import http.HttpResponse;
import utils.HttpRequestParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServerSocket {
    private final ServerSocket serverSocket;
    private final int PORT = 4221;
    ExecutorService executor;

    public HttpServerSocket() throws IOException {
        serverSocket = new ServerSocket(PORT);
        serverSocket.setReuseAddress(true);
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void listen() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                executor.execute(() -> {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        HttpRequest request = HttpRequestParser.parse(in);
                        HttpResponse response = new HttpResponse(request);
                        OutputStream os = clientSocket.getOutputStream();
                        os.write(response.respond().getBytes());
                        os.close();
                    } catch (IOException e) {
                        System.out.println("Error handling request: " + e.getMessage());
                    }
                });
            } catch (IOException e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }
}
