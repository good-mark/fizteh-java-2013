package ru.fizteh.fivt.students.fedoseev.shell;

import java.io.IOException;

public abstract class AbstractCommand implements AbstractShell.Command {
    private String cmdName;
    private int argsCount;

    public AbstractCommand(String cmdName, int argsCount) {
        this.cmdName = cmdName;
        this.argsCount = argsCount;
    }

    public String getCmdName() {
        return cmdName;
    }

    public int getArgsCount() {
        return argsCount;
    }

    public abstract void execute(String[] input, AbstractShell.ShellState state) throws IOException;
}
