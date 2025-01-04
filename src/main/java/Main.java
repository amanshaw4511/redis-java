import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        int DEFAULT_PORT = 6379;
        serverSocket.bind(new InetSocketAddress(DEFAULT_PORT));
        serverSocket.configureBlocking(false);

        Selector selector = Selector.open();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();

            var keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                var key = keyIterator.next();
                keyIterator.remove();

                if (key.isAcceptable()) {
                    SocketChannel clientChannel = serverSocket.accept();
                    clientChannel.configureBlocking(false);
                    clientChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println("Client connected: " + clientChannel.hashCode());
                }

                if (key.isReadable()) {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    processMessage(clientChannel);
                }

            }

        }

    }

    private static void processMessage(SocketChannel client) throws IOException {
        int byteSize = 256;
        ByteBuffer buffer = ByteBuffer.allocate(byteSize);
        StringBuilder sb = new StringBuilder();
        int byteRead = client.read(buffer);
        while (byteRead > 0) {
            buffer.flip();
            sb.append(new String(buffer.array(), 0, byteRead));
            byteRead = client.read(buffer);
        }
        String message = sb.toString();
        System.out.println("Received " + client.hashCode() + " : " + message);

        if (message.toUpperCase().contains("PING")) {
            client.write(ByteBuffer.wrap("+PONG\r\n".getBytes()));
        } else if (message.toUpperCase().contains("DOCS")) {
            client.write(ByteBuffer.wrap("*0\r\n".getBytes()));
        } else {
            client.write(ByteBuffer.wrap("+OK\r\n".getBytes()));
        }


        if (byteRead == -1) {
            client.close();
        }

    }


}
