import lombok.extern.slf4j.Slf4j;
import serialize.RedisSerializer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RedisCommandProcessor {
    private static final String EXPIRY_TOKEN = "PX";
    private final RedisCommandParser commandParser;
    private final RedisSerializer serializer;

    private final Map<String, ValueWithExpiry<String>> map = new HashMap<>();

    public RedisCommandProcessor(RedisCommandParser commandParser, RedisSerializer serializer) {
        this.commandParser = commandParser;
        this.serializer = serializer;
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
        log.debug("Before GET operation : {}", map);
        assertParamsLen(parsedCommand, 2);
        var key = parsedCommand.get(1);

        var value = map.get(key);

        if (value == null) {
            return serializer.str(null);
        }

        if (value.isExpired()) {
            map.remove(key);
            return serializer.str(null);
        }

        return serializer.str(value.value());
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
            map.put(key, ValueWithExpiry.ofNoExpiry(value));

            log.debug("AFTER SET operation : {}", map);
            return serializer.ok();
        }

        assert parsedCommand.get(3).equalsIgnoreCase(EXPIRY_TOKEN);
        var expiryInMillis = Integer.parseInt(parsedCommand.get(4));

        map.put(key, ValueWithExpiry.of(value, LocalDateTime.now().plus(expiryInMillis, ChronoUnit.MILLIS)));
        log.debug("AFTER SET operation : {}", map);
        return serializer.ok();
    }
}
