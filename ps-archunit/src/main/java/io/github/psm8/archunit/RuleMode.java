package io.github.psm8.archunit;

/**
 * Enforcement of a switchable architecture rule.
 */
public enum RuleMode {
	/** Findings are architecture violations. */
	FAIL,
	/** Findings are architecture advisories reported by the advisory rule factories. */
	ADVISORY,
	/** The rule is not evaluated. */
	OFF
}
