import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "of")
@ToString
public class ValueWithExpiry<T> {
    private T value;
    private LocalDateTime expireAfter;

    public boolean isExpired() {
        if (expireAfter == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expireAfter);
    }

    public static<T> ValueWithExpiry<T> ofNoExpiry(T value) {
        return of(value, null);
    }
}
