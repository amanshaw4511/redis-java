import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

//      Uncomment this block to pass the first stage
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
        try {
            serverSocket = new ServerSocket(port);
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);
            // Wait for connection from client.
            clientSocket = serverSocket.accept();

            String message = receiveMessage(clientSocket);
            while (message != null) {
                if (message.contains("PING")) {
                    sendMessage(clientSocket, "+PONG\r\n");
                }
                message = receiveMessage(clientSocket);
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }

    public static String receiveMessage(Socket socket) throws IOException {
        var inStream = socket.getInputStream();
        var reader = new BufferedReader(new InputStreamReader(inStream));
        var message =  reader.readLine();

        System.out.println("received: " + message);
        return message;
    }

    public static void sendMessage(Socket socket, String message) throws IOException {
        System.out.println("sending message " + message);
        var outStream = socket.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
        writer.write(message);
        writer.flush();
        System.out.println("send");
    }
}
