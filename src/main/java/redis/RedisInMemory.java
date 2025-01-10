package redis;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@ToString
public class RedisInMemory {
    private final Map<String, RedisValue> map = new HashMap<>();

    public void set(String key, String value) {
        map.put(key, RedisValue.ofString(value));
    }

    public void set(String key, RedisValue value) {
        map.put(key, value);
    }

    public void setIfExist(String key, String newValue) {
        var value = map.get(key);
        if (value == null) {
            log.info("Value for key {} is not present", key);
            return;
        }

        value.updateValue(newValue);
    }

    public void set(String key, String value, int expiryInMillis) {
        map.put(key, RedisValue.ofString(value, LocalDateTime.now().plus(expiryInMillis, ChronoUnit.MILLIS)));
    }

    public void remove(String key) {
        map.remove(key);
    }

    public Optional<RedisValue> get(String key) {
        var value = map.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value.isExpired()) {
            map.remove(key);
            return Optional.empty();
        }

        return Optional.of(value);
    }

    public Optional<String> getAsString(String key) {
        return get(key)
                .map(RedisValue::asString);
    }
}
