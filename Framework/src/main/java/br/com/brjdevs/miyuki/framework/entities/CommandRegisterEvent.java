package br.com.brjdevs.miyuki.framework.entities;

public class CommandRegisterEvent {
	private final Object command;
	private final String value;

	public CommandRegisterEvent(Object command, String value) {
		this.command = command;
		this.value = value;
	}

	public String getName() {
		return value;
	}

	public Object getCommand() {
		return command;
	}
}
