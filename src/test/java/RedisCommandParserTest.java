import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import redis.RedisCommandParser;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RedisCommandParserTest {

    @ParameterizedTest
    @MethodSource({"params"})
    void parse(String title, String command, List<String> parsedCommand) {
        RedisCommandParser parser = new RedisCommandParser();
        var result = parser.parse(command);
        assertThat(result)
                .isEqualTo(parsedCommand);
    }

    static Stream<Arguments> params() {
        return Stream.of(
                Arguments.of("ECHO", "*2\r\n$4\r\nECHO\r\n$3\r\nhey\r\n", List.of("ECHO", "hey")),
                Arguments.of("PING", "*1\r\n$4\r\nPING\r\n", List.of("PING"))
        );
    }
}