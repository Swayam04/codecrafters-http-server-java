import sockets.HttpServerSocket;


import java.io.IOException;

public class Main {
  public static void main(String[] args) throws IOException {
      HttpServerSocket serverSocket = new HttpServerSocket(args);
      serverSocket.listen();
  }
}
