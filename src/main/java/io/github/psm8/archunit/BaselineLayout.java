package io.github.psm8.archunit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.lang.model.SourceVersion;

/**
 * Immutable Level 1 package vocabulary and rule configuration.
 */
public final class BaselineLayout {
	private final String basePackage;
	private final List<String> outputs;
	private final List<DependencyBan> dependencyBans;
	private final List<String> cyclePatterns;
	private final List<PatternPair> cycleDependencyIgnores;
	private final List<String> publicConfigurationClasses;
	private final List<String> publicConfigurationProperties;
	private final List<String> interfaceBeanReturnTypes;
	private final String outputSuffix;

	private BaselineLayout(Builder builder) {
		basePackage = builder.basePackage;
		outputs = List.copyOf(builder.outputs);
		dependencyBans = List.copyOf(builder.dependencyBans);
		cyclePatterns = List.copyOf(builder.cyclePatterns);
		cycleDependencyIgnores = List.copyOf(builder.cycleDependencyIgnores);
		publicConfigurationClasses = List.copyOf(builder.publicConfigurationClasses);
		publicConfigurationProperties = List.copyOf(builder.publicConfigurationProperties);
		interfaceBeanReturnTypes = List.copyOf(builder.interfaceBeanReturnTypes);
		outputSuffix = builder.outputSuffix;
	}

	public static Builder builder(String basePackage) {
		return new Builder(basePackage);
	}

	public static Builder builder() {
		return new Builder(null);
	}

	public static BaselineLayout of(String basePackage) {
		return builder(basePackage).build();
	}

	public Builder toBuilder() {
		return builder(basePackage)
				.outputs(outputs.toArray(String[]::new))
				.dependencyBans(dependencyBans.toArray(DependencyBan[]::new))
				.cyclePatterns(cyclePatterns.toArray(String[]::new))
				.ignoreCycles(cycleDependencyIgnores)
				.publicConfigurationClasses(publicConfigurationClasses.toArray(String[]::new))
				.publicConfigurationProperties(
						publicConfigurationProperties.toArray(String[]::new))
				.interfaceBeanReturnTypes(interfaceBeanReturnTypes.toArray(String[]::new))
				.outputSuffix(outputSuffix);
	}

	public String basePackage() {
		return basePackage;
	}

	public List<String> outputs() {
		return outputs;
	}

	public List<DependencyBan> dependencyBans() {
		return dependencyBans;
	}

	public List<String> cyclePatterns() {
		return cyclePatterns;
	}

	public List<PatternPair> cycleDependencyIgnores() {
		return cycleDependencyIgnores;
	}

	public List<String> publicConfigurationClasses() {
		return publicConfigurationClasses;
	}

	public List<String> publicConfigurationProperties() {
		return publicConfigurationProperties;
	}

	public List<String> interfaceBeanReturnTypes() {
		return interfaceBeanReturnTypes;
	}

	public String outputSuffix() {
		return outputSuffix;
	}

	public record DependencyBan(
			List<String> sourcePackages,
			List<String> bannedPackages,
			List<PatternPair> ignoredDependencies) {
		public DependencyBan {
			sourcePackages = LayoutSupport.copyRequiredPatterns(sourcePackages, "source package");
			bannedPackages = LayoutSupport.copyRequiredPatterns(bannedPackages, "banned package");
			ignoredDependencies = List.copyOf(
					Objects.requireNonNull(ignoredDependencies, "ignoredDependencies"));
		}

		public static DependencyBan of(String sourcePackage, String... bannedPackages) {
			return of(List.of(sourcePackage), List.of(bannedPackages));
		}

		public static DependencyBan of(String sourcePackage, List<String> bannedPackages) {
			return of(List.of(sourcePackage), bannedPackages);
		}

		public static DependencyBan of(
				List<String> sourcePackages, List<String> bannedPackages) {
			return new DependencyBan(sourcePackages, bannedPackages, List.of());
		}

		public DependencyBan ignoring(String sourcePattern, String targetPattern) {
			List<PatternPair> exceptions = new ArrayList<>(ignoredDependencies);
			exceptions.add(new PatternPair(sourcePattern, targetPattern));
			return new DependencyBan(sourcePackages, bannedPackages, exceptions);
		}
	}

	public record PatternPair(String source, String target) {
		public PatternPair {
			source = LayoutSupport.requiredPattern(source);
			target = LayoutSupport.requiredPattern(target);
		}
	}

	public static final class Builder {
		private String basePackage;
		private final List<String> outputs = new ArrayList<>();
		private final List<DependencyBan> dependencyBans = new ArrayList<>();
		private final List<String> cyclePatterns = new ArrayList<>();
		private final List<PatternPair> cycleDependencyIgnores = new ArrayList<>();
		private final List<String> publicConfigurationClasses = new ArrayList<>();
		private final List<String> publicConfigurationProperties = new ArrayList<>();
		private final List<String> interfaceBeanReturnTypes = new ArrayList<>();
		private String outputSuffix;
		private boolean outputsConfigured;

		private Builder(String basePackage) {
			this.basePackage = basePackage;
		}

		public Builder basePackage(String value) {
			basePackage = value;
			return this;
		}

		String basePackage() {
			return basePackage;
		}

		public Builder outputs(String... values) {
			outputsConfigured = true;
			LayoutSupport.replacePatterns(outputs, values);
			return this;
		}

		public Builder addOutputs(String... values) {
			LayoutSupport.addPatterns(outputs, values);
			return this;
		}

		public Builder dependencyBans(DependencyBan... values) {
			if (values != null) {
				for (DependencyBan value : values) {
					dependencyBans.add(Objects.requireNonNull(value, "dependencyBan"));
				}
			}
			return this;
		}

		public Builder cyclePatterns(String... values) {
			LayoutSupport.replacePatterns(cyclePatterns, values);
			return this;
		}

		public Builder ignoreCycleDependency(String source, String target) {
			cycleDependencyIgnores.add(new PatternPair(source, target));
			return this;
		}

		private Builder ignoreCycles(List<PatternPair> values) {
			cycleDependencyIgnores.addAll(values);
			return this;
		}

		public Builder publicConfigurationClasses(String... values) {
			LayoutSupport.replacePatterns(publicConfigurationClasses, values);
			return this;
		}

		public Builder publicConfigurationProperties(String... values) {
			LayoutSupport.replacePatterns(publicConfigurationProperties, values);
			return this;
		}

		public Builder interfaceBeanReturnTypes(String... values) {
			LayoutSupport.replacePatterns(interfaceBeanReturnTypes, values);
			return this;
		}

		public Builder outputSuffix(String value) {
			outputSuffix = LayoutSupport.optionalText(value);
			return this;
		}

		public BaselineLayout build() {
			LayoutSupport.validateBasePackage(basePackage);
			if (!outputsConfigured) {
				LayoutSupport.prependPatterns(outputs, basePackage + "..");
			}
			outputSuffix = LayoutSupport.defaultText(outputSuffix, "Output");
			if (cyclePatterns.isEmpty()) {
				cyclePatterns.add(basePackage + ".(**)");
			}
			return new BaselineLayout(this);
		}
	}
}
