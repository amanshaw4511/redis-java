import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RedisCommandProcessor {
    private final RedisCommandParser commandParser;

    private Map<String, String> map = new HashMap<>();

    public RedisCommandProcessor(RedisCommandParser commandParser) {
        this.commandParser = commandParser;
    }

    public String process(String inputCommand) {
        var parsedCommand = commandParser.parse(inputCommand);
        var command = RedisCommands.valueOf(parsedCommand.get(0).toUpperCase());

        switch (command) {
            case PING -> {
                return "+PONG\r\n";
            }
            case COMMAND -> {
                return "*0\r\n";
            }
            case ECHO -> {
                return processEcho(parsedCommand);
            }
            case GET -> {
                return processGet(parsedCommand);
            }
            case SET -> {
                return processSet(parsedCommand);
            }
        }

        throw new IllegalArgumentException();
    }

    private String processEcho(List<String> parsedCommand) {
        assertParamsLen(parsedCommand, 2);
        var echoVal = parsedCommand.get(1);
        return "$" +  echoVal.length() + "\r\n" + echoVal + "\r\n";
    }

    private void assertParamsLen(List<String> parsedCommand, int length) {
        if (parsedCommand.size() != length) {
            throw new IllegalArgumentException("Invalid command params length, " + parsedCommand);
        }
    }

    private String processGet(List<String> parsedCommand) {
        assertParamsLen(parsedCommand, 2);
        var key = parsedCommand.get(1);

        String value = map.get(key);
        if (value == null) {
            return "$-1\r\n";
        }
        return "$" + value.length() + "\r\n" + value + "\r\n";
    }

    private String processSet(List<String> parsedCommand) {
        assertParamsLen(parsedCommand, 3);

        var key = parsedCommand.get(1);
        var value = parsedCommand.get(2);

        map.put(key, value);

        return "+OK" + "\r\n";
    }
}
