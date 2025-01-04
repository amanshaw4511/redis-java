import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RedisCommandProcessorTest {

    RedisCommandProcessor redisCommandProcessor;

    @BeforeEach
    void setup() {
        redisCommandProcessor = new RedisCommandProcessor(
                new RedisCommandParser()
        );
    }

    @Test
    void PING_command() {
        var ping = "*1\r\n$4\r\nPING\r\n";

        var response = redisCommandProcessor.process(ping);

        assertThat(response)
                .isEqualTo("+PONG\r\n");
    }

    @Test
    void ECHO_command() {
        var echo = "*2\r\n$4\r\nECHO\r\n$3\r\nhey\r\n";

        var response = redisCommandProcessor.process(echo);

        assertThat(response)
                .isEqualTo("$3\r\nhey\r\n");
    }

}