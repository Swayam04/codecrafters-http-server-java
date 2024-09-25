import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
      int port = 4221;
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");
     try {
       ServerSocket serverSocket = new ServerSocket(port);

       // Since the tester restarts your program quite often, setting SO_REUSEADDR
       // ensures that we don't run into 'Address already in use' errors
       serverSocket.setReuseAddress(true);
       Socket client = serverSocket.accept();
       System.out.println("accepted new connection");

       BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
       String[] input = in.readLine().split(" ");

       String method = input[0];
       String[] path = input[1].split("/");

       OutputStream outputStream = client.getOutputStream();

       if(method.equals("GET") && (path[1].equals("echo") || path[1].isEmpty())) {
           outputStream.write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: 3\r\n\r\n" + path[2]).getBytes());
       } else {
           outputStream.write(("HTTP/1.1 404 Not Found\r\n\r\n").getBytes());
       }
     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
}
