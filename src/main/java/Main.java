import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Server server = new Server();

        while (true) {
            Client client = server.accept();

            Thread.ofVirtual().start(() -> {
                        try {
                            server.processCommand(new Command("", client));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
            );
        }
    }

}
