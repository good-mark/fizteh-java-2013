package ru.fizteh.fivt.students.irinapodorozhnaya.shell;

import java.io.IOException;

public class CommandExit extends AbstractCommand {
	public CommandExit(StateShell st) {
		super(0, st);
	}
	
	public String getName() {
		return "exit";
	}
	
	public void execute(String[] args) throws IOException {
		System.exit(0);
	}
}
