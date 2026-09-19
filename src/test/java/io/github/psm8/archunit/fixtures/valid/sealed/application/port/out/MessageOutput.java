package io.github.psm8.archunit.fixtures.valid.sealed.application.port.out;

public sealed interface MessageOutput
		permits MessageOutput.Displayed {

	record Displayed(String value) implements MessageOutput {
	}
}
