import lombok.extern.slf4j.Slf4j;
import serialize.RedisSerializer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
public class RedisCommandProcessor {
    private static final String EXPIRY_TOKEN = "PX";
    private final RedisCommandParser commandParser;
    private final RedisSerializer serializer;
    private final RedisInMemory memory;


    public RedisCommandProcessor(RedisCommandParser commandParser, RedisSerializer serializer, RedisInMemory memory) {
        this.commandParser = commandParser;
        this.serializer = serializer;
        this.memory = memory;
    }

    public String process(String inputCommand) {
        var parsedCommand = commandParser.parse(inputCommand);
        log.debug("parsed command: {}", parsedCommand);

        var command = RedisCommands.valueOf(parsedCommand.get(0).toUpperCase());

        switch (command) {
            case PING -> {
                return serializer.pong();
            }
            case COMMAND -> {
                return serializer.list(List.of());
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
            case INCR -> {
                return processIncr(parsedCommand);
            }
        }

        throw new IllegalArgumentException();
    }

    private String processEcho(List<String> parsedCommand) {
        assertParamsLen(parsedCommand, 2);
        var echoVal = parsedCommand.get(1);
        return "$" + echoVal.length() + "\r\n" + echoVal + "\r\n";
    }

    private void assertParamsLen(List<String> parsedCommand, int length) {
        if (parsedCommand.size() != length) {
            throw new IllegalArgumentException("Invalid command params length, " + parsedCommand);
        }
    }

    private String processIncr(List<String> parsedCommand) {
        assertParamsLen(parsedCommand, 2);
        var key = parsedCommand.get(1);

        if (memory.get(key).isEmpty()) {
            memory.set(key, "1");
            return serializer.integer(1);
        }

        var value = Integer.parseInt(memory.get(key).get());
        var newValue = value + 1;
        memory.setIfExist(key, Integer.toString(newValue));
        return serializer.integer(newValue);
    }

    private String processGet(List<String> parsedCommand) {
        log.debug("Before GET operation : {}", memory);
        assertParamsLen(parsedCommand, 2);
        var key = parsedCommand.get(1);

        var value = memory.get(key);

        if (value.isEmpty()) {
            return serializer.str(null);
        }

        return serializer.str(value.get());
    }

    private String processSet(List<String> parsedCommand) {
        boolean hasExpiry = false;

        if (parsedCommand.size() > 3) {
            assertParamsLen(parsedCommand, 5);
            hasExpiry = true;
        } else {
            assertParamsLen(parsedCommand, 3);
        }

        var key = parsedCommand.get(1);
        var value = parsedCommand.get(2);

        if (!hasExpiry) {
            memory.set(key, value);

            log.debug("AFTER SET operation : {}", memory);
            return serializer.ok();
        }

        assert parsedCommand.get(3).equalsIgnoreCase(EXPIRY_TOKEN);
        var expiryInMillis = Integer.parseInt(parsedCommand.get(4));

        memory.set(key, value, expiryInMillis);
        log.debug("AFTER SET operation : {}", memory);
        return serializer.ok();
    }
}
