import java.io.*;
import java.net.Socket;

public class Client {
    public final BufferedReader reader;
    public final BufferedWriter writer;
    private final Socket socket;

    public Client(Socket clientSocket) throws IOException {
        this.socket = clientSocket;

        var inStream = socket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inStream));

        var outStream = socket.getOutputStream();
        writer = new BufferedWriter(new OutputStreamWriter(outStream));
    }

    public void start() {
    }

}
