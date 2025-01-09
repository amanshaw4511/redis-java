package redis.commands;

import redis.RedisCommands;
import redis.RedisInMemory;
import redis.RedisValue;

import java.util.List;
import java.util.stream.IntStream;

import static redis.RedisCommands.*;

public class ListCommandHandler {
    private final RedisInMemory memory;

    public ListCommandHandler(RedisInMemory memory) {
        this.memory = memory;
    }

    public boolean isListCommand(RedisCommands command) {
        return List.of(LPUSH, RPUSH, LPOP, RPOP, LLEN, LRANGE)
                .contains(command);
    }

    public String lpop(List<String> parsedCommand) {
        return memory.get(parsedCommand.get(1))
                .map(value -> value.asList().removeLast())
                .orElse(null);
    }

    public String rpop(List<String> parsedCommand) {
        return memory.get(parsedCommand.get(1))
                .map(value -> value.asList().removeFirst())
                .orElse(null);
    }


    /**
     * Return length of array
     * Returns 0 if array not present
     */
    public int llen(List<String> parsedCommand) {
        return memory.get(parsedCommand.get(1))
                .map(value -> value.asList().size())
                .orElse(0);
    }

    public List<String> lrange(List<String> parsedCommand) {
        return lrange(
                parsedCommand.get(1),
                Integer.parseInt(parsedCommand.get(2)),
                Integer.parseInt(parsedCommand.get(3))
        );
    }

    /**
     * Return a view of list.
     * All out of bound indexing are ignored
     *
     * @param start inclusive
     * @param end   inclusive, supports negative indexing
     */
    public List<String> lrange(String key, int start, int end) {
        var valueOpt = memory.get(key);
        if (valueOpt.isEmpty()) {
            return List.of();
        }

        var value = valueOpt.get().asList();
        int n = value.size();

        // handle negative index
        start = Math.max(0, start);
        end = end < 0 ? n + end : end;

        // end index out of bound
        end = Math.min(n - 1, end);

        return IntStream.rangeClosed(start, end)
                .mapToObj(value::get)
                .toList();
    }

    /**
     * Insert a values at head of list.
     * Create a list if not exist.
     *
     * @return list length
     */
    public int rpush(List<String> parsedCommand) {
        return push(parsedCommand.get(1), parsedCommand.subList(2, parsedCommand.size()), true);
    }

    /**
     * Insert a values at head of list.
     * Create a list if not exist.
     *
     * @return list length
     */
    public int lpush(List<String> parsedCommand) {
        return push(parsedCommand.get(1), parsedCommand.subList(2, parsedCommand.size()), false);
    }

    public int push(String key, List<String> values, boolean prepend) {
        var value = memory.get(key)
                .orElseGet(() -> {
                    var val = RedisValue.ofList(List.of());
                    memory.set(key, val);
                    return val;
                })
                .asList();

        if (prepend) {
            values.forEach(value::addFirst);
        } else {
            value.addAll(values);
        }
        return value.size();
    }
}
