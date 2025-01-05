import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RedisInMemory {
    private final Map<String, ValueWithExpiry<String>> map = new HashMap<>();

    public void set(String key, String value) {
        var oldVal = map.get(value);
        if (oldVal == null) {
            map.put(key, ValueWithExpiry.ofNoExpiry(value));
            return;
        }

        oldVal.value(value);
    }

    public void set(String key, String value, int expiryInMillis) {
        map.put(key, ValueWithExpiry.of(value, LocalDateTime.now().plus(expiryInMillis, ChronoUnit.MILLIS)));
    }

    public Optional<String> get(String key) {
        var value = map.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value.isExpired()) {
            map.remove(key);
            return Optional.empty();
        }

        return Optional.of(value.value());
    }
}
