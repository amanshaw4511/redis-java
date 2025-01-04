import java.io.IOException;
import java.net.ServerSocket;


public class Server {
    private final ServerSocket socket;

    public Server() throws IOException {
        int DEFAULT_PORT = 6379;
        socket = new ServerSocket(DEFAULT_PORT);
        // Since the tester restarts your program quite often, setting SO_REUSEADDR
        // ensures that we don't run into 'Address already in use' errors
        socket.setReuseAddress(true);
        // Wait for connection from client.
    }

    public Client accept() throws IOException {
        var clientSocket = socket.accept();
        return new Client(clientSocket);
    }

    public void processCommand(Command command) throws IOException {
        var reader = command.client().reader;
        var writer = command.client().writer;
        //
        String line = reader.readLine();
        while (line != null) {
            System.out.println("received from " + command.client().hashCode() + ": " + line);
            if (line.equalsIgnoreCase("PING")) {
                writer.write("+PONG\n\r");
                writer.flush();
            }

            line = reader.readLine();
        }

    }

}
