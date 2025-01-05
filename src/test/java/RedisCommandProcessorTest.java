import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serialize.RedisSerializer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RedisCommandProcessorTest {
    static final String NL = "\r\n";

    RedisCommandProcessor redisCommandProcessor;

    @BeforeEach
    void setup() {
        redisCommandProcessor = new RedisCommandProcessor(
                new RedisCommandParser(),
                new RedisSerializer(),
                new RedisInMemory()
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
                .isEqualTo("$3\r\nbar\r\n");
    }

    @Test
    @SneakyThrows
    void GET_SET_command_with_expiry() {
        var get = "*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n";
        var set = "*5\r\n$3\r\nSET\r\n$3\r\nfoo\r\n$3\r\nbar\r\n" + "$2" + NL + "px" + NL + "$4" + NL + "1000" + NL;

        assertThat(redisCommandProcessor.process(get))
                .isEqualTo("$-1\r\n"); // null return

        assertThat(redisCommandProcessor.process(set))
                .isEqualTo("+OK\r\n"); // ok

        assertThat(redisCommandProcessor.process(get))
                .isEqualTo("$3\r\nbar\r\n");

        Thread.sleep(1100);

        assertThat(redisCommandProcessor.process(get))
                .isEqualTo("$-1\r\n"); // null return
    }

    @Test
    void INCR_command_when_key_doesnt_exist() {
        var incr = "*2" + NL + "$4" + NL + "INCR" + NL + "$3" + NL + "foo" + NL;

        var get = "*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n";

        assertThat(redisCommandProcessor.process(incr))
                .isEqualTo(":1" + NL);

        assertThat(redisCommandProcessor.process(incr))
                .isEqualTo(":2" + NL);

        assertThat(redisCommandProcessor.process(get))
                .isEqualTo("$1" + NL + "2" + NL);
    }

    @Test
    void INCR_command_when_key_exist() {
        var incr = "*2" + NL + "$4" + NL + "INCR" + NL + "$3" + NL + "foo" + NL;

        var get = "*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n";

        var set = "*3" + NL + "$3" + NL + "SET" + NL + "$3" + NL + "foo" + NL + "$1" + NL + "5" + NL;
        assertThat(redisCommandProcessor.process(set))
                .isEqualTo("+OK\r\n"); // ok

        assertThat(redisCommandProcessor.process(incr))
                .isEqualTo(":6" + NL);

        assertThat(redisCommandProcessor.process(incr))
                .isEqualTo(":7" + NL);

        assertThat(redisCommandProcessor.process(get))
                .isEqualTo("$1" + NL + "7" + NL);
    }

    @Test
    @SneakyThrows
    void INCR_command_expired_value() {
        var incr = "*2" + NL + "$4" + NL + "INCR" + NL + "$3" + NL + "foo" + NL;
        var get = "*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n";
        var set = "*5" + NL + "$3" + NL + "SET" + NL + "$3" + NL + "foo" + NL + "$1" + NL + "5" + NL
                + "$2" + NL + "px" + NL + "$4" + NL + "1000" + NL;

        assertThat(redisCommandProcessor.process(set))
                .isEqualTo("+OK\r\n"); // ok

        assertThat(redisCommandProcessor.process(incr))
                .isEqualTo(":6" + NL);
        assertThat(redisCommandProcessor.process(get))
                .isEqualTo("$1" + NL + "6" + NL);

        Thread.sleep(1200);


        assertThat(redisCommandProcessor.process(get))
                .isEqualTo("$-1\r\n"); // null return
        assertThat(redisCommandProcessor.process(incr))
                .isEqualTo(":1" + NL); // after expiry
        assertThat(redisCommandProcessor.process(get))
                .isEqualTo("$1" + NL + "1" + NL);
    }


    @Test
    void INCR_command_value_is_not_int() {
        var set = serializeInput("SET foo bar");
        var incr = serializeInput("INCR foo");

        assertEquals("+OK" + NL, process(set));
        assertEquals("-ERR value is not an integer or out of range" + NL, process(incr));
    }


    String process(String input) {
        return redisCommandProcessor.process(input);
    }

    String serializeInput(String input) {
        var parts = input.split(" ");

        return "*" + parts.length + NL
                + Stream.of(parts)
                .map(String::strip)
                .map(line -> "$" + line.length() + NL + line)
                .collect(Collectors.joining(NL))
                + NL;
    }

}