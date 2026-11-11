package io.github.psm8.archunit.cli;

import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.freeze.TextFileBasedViolationStore;
import com.tngtech.archunit.library.freeze.ViolationStore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/**
 * Violation store in a consumer-committed directory. It ignores ArchUnit's
 * global store properties, never creates a missing store silently, and
 * re-records every rule when refreezing.
 */
final class CommittedViolationStore implements ViolationStore {
	private static final String STORED_RULES_FILE = "stored.rules";

	private final TextFileBasedViolationStore delegate = new TextFileBasedViolationStore();
	private final Path directory;
	private final boolean refreeze;

	CommittedViolationStore(Path directory, boolean refreeze) {
		this.directory = directory;
		this.refreeze = refreeze;
	}

	@Override
	public void initialize(Properties ignoredGlobalProperties) {
		Properties properties = new Properties();
		properties.setProperty("default.path", directory.toString());
		properties.setProperty("default.allowStoreCreation", Boolean.toString(refreeze));
		if (!refreeze && !Files.isRegularFile(directory.resolve(STORED_RULES_FILE))) {
			throw new ConfigurationException(
					"violation store does not exist: " + directory
							+ " (run once with --refreeze to create it)");
		}
		delegate.initialize(properties);
	}

	/**
	 * Fails for a rule without a stored entry, because ArchUnit would otherwise
	 * record all of its current violations silently.
	 */
	@Override
	public boolean contains(ArchRule rule) {
		if (refreeze) {
			return false;
		}
		if (!delegate.contains(rule)) {
			throw new ConfigurationException(
					"violation store " + directory + " has no entry for rule '"
							+ rule.getDescription() + "' (run with --refreeze to record it)");
		}
		return true;
	}

	@Override
	public void save(ArchRule rule, List<String> violations) {
		delegate.save(rule, violations);
	}

	@Override
	public List<String> getViolations(ArchRule rule) {
		return delegate.getViolations(rule);
	}
}
