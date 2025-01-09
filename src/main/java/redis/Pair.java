package redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "of")
@ToString
public class Pair<T1, T2> {
    private T1 v1;
    private T2 v2;
}
