import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RedisCommandProcessorTest {

    RedisCommandProcessor redisCommandProcessor;

    @BeforeEach
    void setup() {
        redisCommandProcessor = new RedisCommandProcessor();
    }

    @Test
    void PING_command() {
        var ping = "*1\r\n$4\r\nPING\r\n";

        var response = redisCommandProcessor.process(ping);

        assertThat(response)
                .isEqualTo("+PONG\r\n");
    }

}