package redis;

import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class RedisValue {
    enum Type {STRING, LIST}

    ;

    private Type type;
    private Object value;
    private LocalDateTime expireAfter;


    public void updateValue(String value) {
        validateType(Type.STRING);
        setValue(value);
    }

    private void validateType(Type type) {
        if (this.type != type) {
            throw new IllegalArgumentException("value is not string, " + value);
        }
    }

    public String asString() {
        validateType(Type.STRING);
        return (String) value;
    }

    public LinkedList<String> asList() {
        validateType(Type.LIST);
        return (LinkedList<String>) value;
    }

    public boolean isExpired() {
        if (expireAfter == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expireAfter);
    }

    public static RedisValue ofString(String value) {
        return new RedisValue(Type.STRING, value, null);
    }

    public static RedisValue ofString(String value, LocalDateTime expireAfter) {
        return new RedisValue(Type.STRING, value, expireAfter);
    }

    public static RedisValue ofList(List<String> value) {
        return new RedisValue(Type.LIST, new LinkedList<>(value), null);
    }

    public static RedisValue ofList(List<String> value, LocalDateTime expireAfter) {
        return new RedisValue(Type.LIST, value,  expireAfter);
    }

}
