package io.github.psm8.archunit.fixtures.valid.sealed.application.port.in;

public sealed interface AuthenticationResult
		permits AuthenticationResult.Authenticated, AuthenticationResult.Rejected {

	record Authenticated() implements AuthenticationResult {
	}

	record Rejected(String reason) implements AuthenticationResult {
	}
}
