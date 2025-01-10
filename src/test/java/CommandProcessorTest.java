import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.CommandProcessor;
import redis.RedisCommandParser;
import redis.RedisInMemory;
import redis.serialize.RedisSerializer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandProcessorTest {
    static final String NL = "\r\n";

    CommandProcessor commandProcessor;
    RedisSerializer serializer;

    @BeforeEach
    void setup() {
        commandProcessor = new CommandProcessor(
                new RedisCommandParser(),
                new RedisSerializer(),
                new RedisInMemory()
        );
        serializer = new RedisSerializer();
    }

    @Test
    void PING_command() {
        var ping = "*1\r\n$4\r\nPING\r\n";

        var response = commandProcessor.process(ping);

        assertThat(response)
                .isEqualTo("+PONG\r\n");
    }

    @Test
    void ECHO_command() {
        var echo = "*2\r\n$4\r\nECHO\r\n$3\r\nhey\r\n";

        var response = commandProcessor.process(echo);

        assertThat(response)
                .isEqualTo("$3\r\nhey\r\n");
    }

    @Test
    void GET_SET_command() {
        var get = "*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n";
        var set = "*3\r\n$3\r\nSET\r\n$3\r\nfoo\r\n$3\r\nbar\r\n";

        assertThat(commandProcessor.process(get))
                .isEqualTo("$-1\r\n"); // null return

        assertThat(commandProcessor.process(set))
                .isEqualTo("+OK\r\n"); // ok

        assertThat(commandProcessor.process(get))
                .isEqualTo("$3\r\nbar\r\n");
    }

    @Test
    @SneakyThrows
    void GET_SET_command_with_expiry() {
        var get = "*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n";
        var set = "*5\r\n$3\r\nSET\r\n$3\r\nfoo\r\n$3\r\nbar\r\n" + "$2" + NL + "px" + NL + "$4" + NL + "1000" + NL;

        assertThat(commandProcessor.process(get))
                .isEqualTo("$-1\r\n"); // null return

        assertThat(commandProcessor.process(set))
                .isEqualTo("+OK\r\n"); // ok

        assertThat(commandProcessor.process(get))
                .isEqualTo("$3\r\nbar\r\n");

        Thread.sleep(1100);

        assertThat(commandProcessor.process(get))
                .isEqualTo("$-1\r\n"); // null return
    }

    @Test
    void INCR_command_when_key_doesnt_exist() {
        var incr = "*2" + NL + "$4" + NL + "INCR" + NL + "$3" + NL + "foo" + NL;

        var get = "*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n";

        assertThat(commandProcessor.process(incr))
                .isEqualTo(":1" + NL);

        assertThat(commandProcessor.process(incr))
                .isEqualTo(":2" + NL);

        assertThat(commandProcessor.process(get))
                .isEqualTo("$1" + NL + "2" + NL);
    }

    @Test
    void INCR_command_when_key_exist() {
        var incr = "*2" + NL + "$4" + NL + "INCR" + NL + "$3" + NL + "foo" + NL;

        var get = "*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n";

        var set = "*3" + NL + "$3" + NL + "SET" + NL + "$3" + NL + "foo" + NL + "$1" + NL + "5" + NL;
        assertThat(commandProcessor.process(set))
                .isEqualTo("+OK\r\n"); // ok

        assertThat(commandProcessor.process(incr))
                .isEqualTo(":6" + NL);

        assertThat(commandProcessor.process(incr))
                .isEqualTo(":7" + NL);

        assertThat(commandProcessor.process(get))
                .isEqualTo("$1" + NL + "7" + NL);
    }

    @Test
    @SneakyThrows
    void INCR_command_expired_value() {
        var incr = "*2" + NL + "$4" + NL + "INCR" + NL + "$3" + NL + "foo" + NL;
        var get = "*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n";
        var set = "*5" + NL + "$3" + NL + "SET" + NL + "$3" + NL + "foo" + NL + "$1" + NL + "5" + NL
                + "$2" + NL + "px" + NL + "$4" + NL + "1000" + NL;

        assertThat(commandProcessor.process(set))
                .isEqualTo("+OK\r\n"); // ok

        assertThat(commandProcessor.process(incr))
                .isEqualTo(":6" + NL);
        assertThat(commandProcessor.process(get))
                .isEqualTo("$1" + NL + "6" + NL);

        Thread.sleep(1200);


        assertThat(commandProcessor.process(get))
                .isEqualTo("$-1\r\n"); // null return
        assertThat(commandProcessor.process(incr))
                .isEqualTo(":1" + NL); // after expiry
        assertThat(commandProcessor.process(get))
                .isEqualTo("$1" + NL + "1" + NL);
    }


    @Test
    void INCR_command_value_is_not_int() {
        var set = serializeInput("SET foo bar");
        var incr = serializeInput("INCR foo");

        assertEquals("+OK" + NL, process(set));
        assertEquals("-ERR value is not an integer or out of range" + NL, process(incr));
    }

    @Test
    void LPUSH_command_should_create_list_when_not_present() {
        assertEquals(":3" + NL, processS("LPUSH l a b c"));
        assertEquals(serializeInput("a b c"), processS("LRANGE l 0 -1"));
    }

    @Test
    void LPUSH_command_should_append_when_present() {
        assertEquals(":3" + NL, processS("LPUSH l a b c"));
        assertEquals(":5" + NL, processS("LPUSH l d e"));
        assertEquals(serializeInput("a b c d e"), processS("LRANGE l 0 -1"));
    }

    @Test
    void RPUSH_command_should_create_list_when_not_present() {
        assertEquals(":3" + NL, processS("RPUSH l a b c"));
        assertEquals(serializeInput("c b a"), processS("LRANGE l 0 -1"));
    }

    @Test
    void RLPUSH_command_should_prepend_when_present() {
        assertEquals(":3" + NL, processS("RPUSH l a b c"));
        assertEquals(":5" + NL, processS("RPUSH l d e"));
        assertEquals(serializeInput("e d c b a"), processS("LRANGE l 0 -1"));
    }

    @Test
    void LRANGE_command() {
        assertEquals(serializeInput(""), processS("LRANGE l 0 -1")); // empty arr when not exist
        assertEquals(":5" + NL, processS("LPUSH l a b c d e"));
        assertEquals(serializeInput("a b c d e"), processS("LRANGE l 0 -1"));


        // slicing
        assertEquals(serializeInput("b c d e"), processS("LRANGE l 1 -1"));
        assertEquals(serializeInput("a b c d"), processS("LRANGE l 0 -2"));
        assertEquals(serializeInput("b c d"), processS("LRANGE l 1 -2"));
        assertEquals(serializeInput("b c d"), processS("LRANGE l 1 3"));
        assertEquals(serializeInput("a b c d e"), processS("LRANGE l 0 4"));

        // ignore out of bound indices
        assertEquals(serializeInput("a b c d e"), processS("LRANGE l -100 -1"));
        assertEquals(serializeInput("a b c d e"), processS("LRANGE l 0 500"));
        assertEquals(serializeInput("a b c d e"), processS("LRANGE l -100 500"));
    }

    @Test
    void LLEN_command() {
        assertEquals(":0" + NL, processS("LLEN l"));

        assertEquals(":3" + NL, processS("LPUSH l a b c"));
        assertEquals(":3" + NL, processS("LLEN l"));

        assertEquals(":5" + NL, processS("LPUSH l d e"));
        assertEquals(":5" + NL, processS("LLEN l"));
    }

    @Test
    void LPOP() {
        assertEquals("$-1" + NL, processS("LPOP l"));

        assertEquals(":5" + NL, processS("LPUSH l a b c d e"));

        assertEquals("$1" + NL + "e" + NL, processS("LPOP l"));
        assertEquals(serializeInput("a b c d"), processS("LRANGE l 0 -1"));

        assertEquals("$1" + NL + "d" + NL, processS("LPOP l"));
        assertEquals(serializeInput("a b c"), processS("LRANGE l 0 -1"));
    }

    @Test
    void RPOP() {
        assertEquals("$-1" + NL, processS("RPOP l"));

        assertEquals(":5" + NL, processS("LPUSH l a b c d e"));

        assertEquals("$1" + NL + "a" + NL, processS("RPOP l"));
        assertEquals(serializeInput("b c d e"), processS("LRANGE l 0 -1"));

        assertEquals("$1" + NL + "b" + NL, processS("RPOP l"));
        assertEquals(serializeInput("c d e"), processS("LRANGE l 0 -1"));
    }

    @Test
    void LPOP_delete_list_when_empty() {
        assertEquals(":1" + NL, processS("LPUSH l a"));
        assertEquals("$1" + NL + "a" + NL, processS("LPOP l"));
        assertEquals("$-1" + NL, processS("GET l")); // null
    }


    String processS(String input) {
        return commandProcessor.process(serializeInput(input));
    }

    String process(String input) {
        return commandProcessor.process(input);
    }

    String serializeInput(String input) {
        if (input.isBlank()) {
            return "*0" + NL;
        }
        var parts = input.split(" ");

        return "*" + parts.length + NL
                + Stream.of(parts)
                .map(String::strip)
                .map(line -> "$" + line.length() + NL + line)
                .collect(Collectors.joining(NL))
                + NL;
    }

}