package io.github.psm8.archunit.fixtures.switchable.builder.named.model;

public final class ResponseBuilder {
	private String prefix = "";

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public Response create(String body) {
		return new Response(prefix + body);
	}
}
