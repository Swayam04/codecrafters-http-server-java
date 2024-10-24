package sockets;

import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.HttpRequestParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServerSocket {
    private final ServerSocket serverSocket;
    private final int PORT = 4221;
    private final ExecutorService executor;
    private static final Logger logger = LoggerFactory.getLogger(HttpServerSocket.class);
    private final String[] commandLineArgs;

    public HttpServerSocket(String[] args) throws IOException {
        serverSocket = new ServerSocket(PORT);
        serverSocket.setReuseAddress(true);
        commandLineArgs = Arrays.copyOfRange(args, 0, args.length);
        executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void listen() {
        logger.info("Listening on port {}", PORT);
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                String clientIP = clientSocket.getInetAddress().getHostAddress();
                logger.info("Accepted connection from {}", clientIP);
                executor.execute(() -> {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        HttpRequest request = HttpRequestParser.parse(in);
                        HttpResponse response = new HttpResponse(request, commandLineArgs);
                        OutputStream os = clientSocket.getOutputStream();
                        os.write(response.writeStatusAndHeaders().getBytes());
                        os.write(response.writeBody());
                        logger.info("Request completed: {} {}, Status: {}", request.getMethod(), request.getPath(), response.getStatus());
                        os.close();
                    } catch (IOException e) {
                        logger.error("Error parsing client request: {}", e.getMessage());
                    }
                });
            } catch (IOException e) {
                logger.error("Error accepting client connection: {}" ,e.getMessage());
            }
        }
    }
}
