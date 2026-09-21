package io.github.psm8.archunit.cli;

import java.util.List;

record ArchitectureReport(
		String version,
		String tier,
		List<String> input,
		boolean pass,
		List<String> violations) {
	String render(ReportFormat format) {
		return format == ReportFormat.JSON ? json() : text();
	}

	private String text() {
		StringBuilder output = new StringBuilder();
		output.append("version: ").append(version).append('\n');
		output.append("tier: ").append(tier).append('\n');
		output.append("input:\n");
		for (String path : input) {
			output.append("- ").append(path).append('\n');
		}
		output.append("pass: ").append(pass).append('\n');
		output.append("violations:\n");
		for (String violation : violations) {
			output.append("- ").append(violation).append('\n');
		}
		return output.toString();
	}

	private String json() {
		return "{\"version\":" + quote(version)
				+ ",\"tier\":" + quote(tier)
				+ ",\"input\":" + strings(input)
				+ ",\"pass\":" + pass
				+ ",\"violations\":" + strings(violations) + "}\n";
	}

	private static String strings(List<String> values) {
		StringBuilder output = new StringBuilder("[");
		for (int index = 0; index < values.size(); index++) {
			if (index > 0) {
				output.append(',');
			}
			output.append(quote(values.get(index)));
		}
		return output.append(']').toString();
	}

	private static String quote(String value) {
		return '"' + value
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\b", "\\b")
				.replace("\f", "\\f")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t")
				+ '"';
	}
}
