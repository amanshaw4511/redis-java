import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serialize.RedisSerializer;

import static org.assertj.core.api.Assertions.assertThat;

class RedisCommandProcessorTest {

    RedisCommandProcessor redisCommandProcessor;

    @BeforeEach
    void setup() {
        redisCommandProcessor = new RedisCommandProcessor(
                new RedisCommandParser(),
                new RedisSerializer()
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

    @Test
    void GET_SET_command() {
        var get = "*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n";
        var set = "*3\r\n$3\r\nSET\r\n$3\r\nfoo\r\n$3\r\nbar\r\n";

        assertThat(redisCommandProcessor.process(get))
                .isEqualTo("$-1\r\n"); // null return

        assertThat(redisCommandProcessor.process(set))
                .isEqualTo("+OK\r\n"); // ok

        assertThat(redisCommandProcessor.process(get))
                .isEqualTo("$3\r\nbar\r\n"); // null return
    }

}