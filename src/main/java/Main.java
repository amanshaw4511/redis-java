import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static BufferedReader reader;
    private static BufferedWriter writer;

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

            setReader(clientSocket);
            setWriter(clientSocket);

            String line = reader.readLine();
            while (line != null) {
                System.out.println("Received: " + line);
                if (line.equalsIgnoreCase("PING")) {
                    writer.write("+PONG\n\r");
                    writer.flush();
                }

                line = reader.readLine();
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

    public static void setReader(Socket socket) throws IOException {
        var inStream = socket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inStream));
    }

    public static void setWriter(Socket socket) throws IOException {
        var outStream = socket.getOutputStream();
        writer = new BufferedWriter(new OutputStreamWriter(outStream));
    }
}
