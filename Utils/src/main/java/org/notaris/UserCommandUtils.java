package org.notaris;

public class UserCommandUtils {

    public static String[] readCommand(String command) {
        String commandType = command.contains(" ") ? command.substring(0, command.indexOf(' ')).trim() : command.trim();
        String rightPart = command.contains(" ") ? command.substring(command.indexOf(' ') + 1).trim() : "";
        return new String[]{commandType, rightPart};
    }

}
