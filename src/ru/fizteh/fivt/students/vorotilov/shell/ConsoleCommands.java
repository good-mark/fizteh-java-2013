package ru.fizteh.fivt.students.vorotilov.shell;

import java.io.IOException;

public abstract class ConsoleCommands {
    public abstract String[] getNextCommand() throws IOException, NoNextCommand;

    protected String[] parseCommand(String input) {
        StringBuilder notParsedCommand = new StringBuilder(input);
        int i = 0;
        while (i < notParsedCommand.length() && notParsedCommand.charAt(i) == ' ') {
            ++i;
        }
        notParsedCommand.delete(0, i);
        int indexOfTwoWhitespaces = notParsedCommand.indexOf("  ");
        while (indexOfTwoWhitespaces != -1) {
            notParsedCommand.delete(indexOfTwoWhitespaces, indexOfTwoWhitespaces + 1);
        }
        return notParsedCommand.toString().split("[ ]+");
    }
}

