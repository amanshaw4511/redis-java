package redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RedisCommandParser {

    public List<String> parse(String command) {
        //*2\r\n$4\r\nECHO\r\n$3\r\nhey\r\n
        Scanner sc = new Scanner(command);
        if (!sc.hasNextLine()) {
            throw new IllegalArgumentException("Invalid command, "+ command);
        }

        String line1 = sc.nextLine();

        if (!line1.startsWith("*")) {
            throw new IllegalArgumentException("'*' expected as first character, " + command);
        }

        var len = getLen(line1);

        if (len <= 0) {
            throw new IllegalArgumentException("Invalid parameter length "+ len);
        }

        //\r\n$4\r\nECHO\r\n$3\r\nhey\r\n
        List<String> parsedCommand = new ArrayList<>();
        for (int i=0; i<len; i++) {
            if (!sc.hasNextLine()) {
                throw new IllegalArgumentException();
            }
            int charLen = Integer.parseInt(sc.nextLine().substring(1));
            if (!sc.hasNextLine()) {
                throw new IllegalArgumentException();
            }
            var param = sc.nextLine();
            if (param.length() != charLen) {
                throw new IllegalArgumentException(String.format(
                        "Character length is not matching for param: %s, length: %s",
                        param,
                        charLen
                ));
            }
            parsedCommand.add(param);
        }

        return parsedCommand;
    }

    private int getLen(String line) {
        try {
            String lenStr = line.substring(1);
            return Integer.parseInt(lenStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse length of arguments in first line, " + line);
        }
    }
}
