package io.github.psm8.archunit.fixtures.composition.boot;

import io.github.psm8.archunit.fixtures.composition.boot.adapter.out.BootGateway;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public final class DirectBootRoot {
	private final BootGateway gateway;

	public DirectBootRoot(BootGateway gateway) {
		this.gateway = gateway;
	}

	public static void main(String[] args) {
	}

	public BootGateway gateway() {
		return gateway;
	}
}
