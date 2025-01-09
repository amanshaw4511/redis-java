import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

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

}
