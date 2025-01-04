import lombok.extern.slf4j.Slf4j;
import serialize.RedisSerializer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

@Slf4j
public class Main {
    private static RedisCommandProcessor processor = new RedisCommandProcessor(
            new RedisCommandParser(),
            new RedisSerializer()
    );

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        int DEFAULT_PORT = 6379;
        serverSocket.bind(new InetSocketAddress(DEFAULT_PORT));
        serverSocket.configureBlocking(false);

        Selector selector = Selector.open();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT); // on accept event put the event to selector

        while (true) {
            selector.select(); // blocking till an event

            var keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                var key = keyIterator.next();
                keyIterator.remove();

                if (key.isAcceptable()) {
                    SocketChannel clientChannel = serverSocket.accept();
                    clientChannel.configureBlocking(false);
                    clientChannel.register(selector, SelectionKey.OP_READ); // on read is available put the event to selector
                    log.info("Client connected {}", clientChannel.getRemoteAddress());
                }

                if (key.isReadable()) {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    handleClientMessage(clientChannel);
                }

            }

        }

    }

    private static Pair<String, Boolean> readMessage(SocketChannel client) throws IOException {
        int byteSize = 256;
        ByteBuffer buffer = ByteBuffer.allocate(byteSize);
        StringBuilder sb = new StringBuilder();
        int byteRead = client.read(buffer);
        while (byteRead > 0) {
            buffer.flip();
            sb.append(new String(buffer.array(), 0, byteRead));
            byteRead = client.read(buffer);
        }
        return Pair.of(sb.toString(), byteRead != -1);
    }


    private static void writeMessage(SocketChannel client, String message) throws IOException {
        client.write(ByteBuffer.wrap(message.getBytes()));
    }

    private static void handleClientMessage(SocketChannel client) throws IOException {
        var pair = readMessage(client);
        String message = pair.v1();
        boolean isClientConnected =  pair.v2();

        log.debug("Received message from client {} :\n{}", client.getRemoteAddress(), message);

        var response = processor.process(message);

        writeMessage(client, response);

        if (!isClientConnected) {
            client.close();
        }

    }

}
