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

public class HttpServerSocket {
    private final ServerSocket serverSocket;
    private final int PORT = 4221;

    public HttpServerSocket() throws IOException {
        serverSocket = new ServerSocket(PORT);
        serverSocket.setReuseAddress(true);
    }

    public void listen() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            HttpRequest request = HttpRequestParser.parse(in);
            HttpResponse response = new HttpResponse(request);
            OutputStream os = clientSocket.getOutputStream();
            os.write(response.respond().getBytes());
            os.close();
        }
    }
}
