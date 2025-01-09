package redis.serialize;

import java.util.List;

public class RedisSerializer {
    private static final String NL = "\r\n";

    public String list(List<String> list) {
        if (list.isEmpty()) {
            return "*0" + NL;
        }
        var len = list.size();
        StringBuilder sb = new StringBuilder();
        for (var element : list) {
            sb.append(str(element));
        }

        return "*" + len + NL +  sb;
    }

    public String integer(int value) {
        return ":" + value + NL;
    }

    public String err(String message) {
        return "-ERR " + message + NL;
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
