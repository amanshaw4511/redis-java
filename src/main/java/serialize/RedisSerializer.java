package serialize;

import java.util.List;

public class RedisSerializer {
    private static final String NL = "\r\n";

    public String list(List<?> list) {
        if (list.isEmpty()) {
            return "*0" + NL;
        }
        var len = list.size();
        StringBuilder sb = new StringBuilder();
        for (var element : list) {
            var out = switch (element) {
                case String s -> str(s);
                default -> throw new IllegalArgumentException("Not yet implemented");
            };
            sb.append(out).append(NL);
        }

        return "*" + len + sb;
    }

    public String str(String str) {
        if (str == null) {
            return "$-1" + NL;
        }

        return "$" + str.length() + NL + str + NL;
    }

    public String ok() {
        return "+OK" + NL;
    }

    public String pong() {
        return "+PONG" + NL;
    }

}
