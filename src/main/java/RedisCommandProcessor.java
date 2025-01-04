public class RedisCommandProcessor {
    private final RedisCommandParser commandParser;

    public RedisCommandProcessor(RedisCommandParser commandParser) {
        this.commandParser = commandParser;
    }

    public String process(String inputCommand) {
        var parsedCommand = commandParser.parse(inputCommand);
        var command = RedisCommands.valueOf(parsedCommand.get(0).toUpperCase());

        switch (command) {
            case PING -> {
                return "+PONG\r\n";
            }
            case COMMAND -> {
                return "*0\r\n";
            }
        }

        throw new IllegalArgumentException();
    }
}
