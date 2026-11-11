package io.github.psm8.archunit.fixtures.switchable.setter.jdk.model;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;

public class LogHandler extends ConsoleHandler {
	private String own;

	@Override
	public synchronized void setFormatter(Formatter formatter) {
		super.setFormatter(formatter);
	}

	public void setOwn(String own) {
		this.own = own;
	}
}
