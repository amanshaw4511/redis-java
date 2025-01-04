import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "of")
public class Pair<T1, T2> {
    private T1 v1;
    private T2 v2;
}
