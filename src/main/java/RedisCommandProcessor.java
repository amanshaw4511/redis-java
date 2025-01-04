public class RedisCommandProcessor {

    public String process(String command) {
        command = command.toUpperCase();

        if (command.contains("PING")) {
            return "+PONG\r\n";
        } else if (command.contains("DOCS")) {
            return "*0\r\n";
        } else {
            return "+OK\r\n";
        }
    }
}
