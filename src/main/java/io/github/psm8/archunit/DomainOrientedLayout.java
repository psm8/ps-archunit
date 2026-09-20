package io.github.psm8.archunit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Immutable cumulative Level 2 package vocabulary and rule configuration.
 */
public final class DomainOrientedLayout {
	private final BaselineLayout baseline;
	private final List<String> domain;
	private final List<String> applicationPackages;
	private final List<String> apiPackages;
	private final List<String> infrastructurePackages;
	private final List<String> domainModelFrameworkPackages;
	private final List<String> frameworkDependencyPackages;
	private final List<BaselineLayout.PatternPair> dependencyDirectionIgnores;

	private DomainOrientedLayout(Builder builder) {
		baseline = builder.baseline.build();
		domain = List.copyOf(builder.domain);
		applicationPackages = List.copyOf(builder.applicationPackages);
		apiPackages = List.copyOf(builder.apiPackages);
		infrastructurePackages = List.copyOf(builder.infrastructurePackages);
		domainModelFrameworkPackages = List.copyOf(builder.domainModelFrameworkPackages);
		frameworkDependencyPackages = List.copyOf(builder.frameworkDependencyPackages);
		dependencyDirectionIgnores = List.copyOf(builder.dependencyDirectionIgnores);
	}

	public static Builder builder(String basePackage) {
		return new Builder(BaselineLayout.builder(basePackage));
	}

	public static Builder builder(BaselineLayout baseline) {
		Objects.requireNonNull(baseline, "baseline");
		return new Builder(copyBaseline(baseline));
	}

	public static Builder builder() {
		return builder((String) null);
	}

	public static DomainOrientedLayout of(String basePackage) {
		return builder(basePackage).build();
	}

	public static DomainOrientedLayout of(BaselineLayout baseline) {
		return builder(baseline).build();
	}

	public Builder toBuilder() {
		Builder copy = builder(baseline)
				.domain(domain.toArray(String[]::new))
				.applicationPackages(applicationPackages.toArray(String[]::new))
				.apiPackages(apiPackages.toArray(String[]::new))
				.infrastructurePackages(infrastructurePackages.toArray(String[]::new))
				.domainModelFrameworkPackages(
						domainModelFrameworkPackages.toArray(String[]::new))
				.frameworkDependencyPackages(
						frameworkDependencyPackages.toArray(String[]::new));
		copy.dependencyDirectionIgnores.addAll(dependencyDirectionIgnores);
		copy.domainConfigured = true;
		copy.applicationConfigured = true;
		copy.apiConfigured = true;
		copy.infrastructureConfigured = true;
		copy.domainModelFrameworkConfigured = true;
		return copy;
	}

	public BaselineLayout baseline() {
		return baseline;
	}

	public String basePackage() {
		return baseline.basePackage();
	}

	public List<String> outputs() {
		return baseline.outputs();
	}

	public List<BaselineLayout.DependencyBan> dependencyBans() {
		return baseline.dependencyBans();
	}

	public List<String> cyclePatterns() {
		return baseline.cyclePatterns();
	}

	public List<BaselineLayout.PatternPair> cycleDependencyIgnores() {
		return baseline.cycleDependencyIgnores();
	}

	public List<String> publicConfigurationClasses() {
		return baseline.publicConfigurationClasses();
	}

	public List<String> publicConfigurationProperties() {
		return baseline.publicConfigurationProperties();
	}

	public List<String> allowedApplicationInterfaceBeanTypes() {
		return baseline.allowedApplicationInterfaceBeanTypes();
	}

	public String outputSuffix() {
		return baseline.outputSuffix();
	}

	public List<String> domain() {
		return domain;
	}

	public List<String> applicationPackages() {
		return applicationPackages;
	}

	public List<String> apiPackages() {
		return apiPackages;
	}

	public List<String> infrastructurePackages() {
		return infrastructurePackages;
	}

	public List<String> domainModelFrameworkPackages() {
		return domainModelFrameworkPackages;
	}

	public List<String> frameworkDependencyPackages() {
		return frameworkDependencyPackages;
	}

	public List<BaselineLayout.PatternPair> dependencyDirectionIgnores() {
		return dependencyDirectionIgnores;
	}

	public static final class Builder {
		private final BaselineLayout.Builder baseline;
		private final List<String> domain = new ArrayList<>();
		private final List<String> applicationPackages = new ArrayList<>();
		private final List<String> apiPackages = new ArrayList<>();
		private final List<String> infrastructurePackages = new ArrayList<>();
		private final List<String> domainModelFrameworkPackages = new ArrayList<>();
		private final List<String> frameworkDependencyPackages = new ArrayList<>();
		private final List<BaselineLayout.PatternPair> dependencyDirectionIgnores =
				new ArrayList<>();
		private boolean domainConfigured;
		private boolean applicationConfigured;
		private boolean apiConfigured;
		private boolean infrastructureConfigured;
		private boolean domainModelFrameworkConfigured;
		private boolean frameworkDependencyConfigured;

		private Builder(BaselineLayout.Builder baseline) {
			this.baseline = baseline;
		}

		public Builder basePackage(String value) {
			baseline.basePackage(value);
			return this;
		}

		public Builder outputs(String... values) {
			baseline.outputs(values);
			return this;
		}

		public Builder addOutputs(String... values) {
			baseline.addOutputs(values);
			return this;
		}

		public Builder dependencyBans(BaselineLayout.DependencyBan... values) {
			baseline.dependencyBans(values);
			return this;
		}

		public Builder cyclePatterns(String... values) {
			baseline.cyclePatterns(values);
			return this;
		}

		public Builder ignoreDependency(String source, String target) {
			dependencyDirectionIgnores.add(new BaselineLayout.PatternPair(source, target));
			return this;
		}

		public Builder ignoreCycleDependency(String source, String target) {
			baseline.ignoreCycleDependency(source, target);
			return this;
		}

		public Builder publicConfigurationClasses(String... values) {
			baseline.publicConfigurationClasses(values);
			return this;
		}

		public Builder publicConfigurationProperties(String... values) {
			baseline.publicConfigurationProperties(values);
			return this;
		}

		public Builder allowedApplicationInterfaceBeanTypes(String... values) {
			baseline.allowedApplicationInterfaceBeanTypes(values);
			return this;
		}

		public Builder outputSuffix(String value) {
			baseline.outputSuffix(value);
			return this;
		}

		public Builder domain(String... values) {
			domainConfigured = true;
			LayoutSupport.replacePatterns(domain, values);
			return this;
		}

		public Builder addDomains(String... values) {
			LayoutSupport.addPatterns(domain, values);
			return this;
		}

		public Builder applicationPackages(String... values) {
			applicationConfigured = true;
			LayoutSupport.replacePatterns(applicationPackages, values);
			return this;
		}

		public Builder addApplicationPackages(String... values) {
			LayoutSupport.addPatterns(applicationPackages, values);
			return this;
		}

		public Builder apiPackages(String... values) {
			apiConfigured = true;
			LayoutSupport.replacePatterns(apiPackages, values);
			return this;
		}

		public Builder addApiPackages(String... values) {
			LayoutSupport.addPatterns(apiPackages, values);
			return this;
		}

		public Builder infrastructurePackages(String... values) {
			infrastructureConfigured = true;
			LayoutSupport.replacePatterns(infrastructurePackages, values);
			return this;
		}

		public Builder addInfrastructurePackages(String... values) {
			LayoutSupport.addPatterns(infrastructurePackages, values);
			return this;
		}

		public Builder domainModelFrameworkPackages(String... values) {
			domainModelFrameworkConfigured = true;
			LayoutSupport.replacePatterns(domainModelFrameworkPackages, values);
			return this;
		}

		public Builder addDomainModelFrameworkPackages(String... values) {
			LayoutSupport.addPatterns(domainModelFrameworkPackages, values);
			return this;
		}

		public Builder frameworkDependencyPackages(String... values) {
			frameworkDependencyConfigured = true;
			LayoutSupport.replacePatterns(frameworkDependencyPackages, values);
			return this;
		}

		public Builder addFrameworkDependencyPackages(String... values) {
			LayoutSupport.addPatterns(frameworkDependencyPackages, values);
			return this;
		}

		public DomainOrientedLayout build() {
			if (!domainConfigured) {
				LayoutSupport.prependPatterns(domain, baselinePackage() + ".domain..");
			}
			if (!applicationConfigured) {
				LayoutSupport.prependPatterns(
						applicationPackages, baselinePackage() + ".application..");
			}
			if (!apiConfigured) {
				LayoutSupport.prependPatterns(apiPackages, baselinePackage() + ".api..");
			}
			if (!infrastructureConfigured) {
				LayoutSupport.prependPatterns(
						infrastructurePackages, baselinePackage() + ".infrastructure..");
			}
			if (!domainModelFrameworkConfigured) {
				LayoutSupport.prependPatterns(
						domainModelFrameworkPackages,
						"jakarta.persistence..",
						"jakarta.validation..",
						"javax.persistence..",
						"javax.validation..",
						"com.fasterxml.jackson.annotation..");
			}
			if (!frameworkDependencyConfigured) {
				LayoutSupport.prependPatterns(
						frameworkDependencyPackages,
						"org.springframework..",
						"jakarta..",
						"javax..",
						"com.fasterxml.jackson..");
			}
			for (String modelFrameworkPackage : domainModelFrameworkPackages) {
				if (!frameworkDependencyPackages.contains(modelFrameworkPackage)) {
					frameworkDependencyPackages.add(modelFrameworkPackage);
				}
			}
			return new DomainOrientedLayout(this);
		}

		private String baselinePackage() {
			return baseline.basePackage();
		}

		String basePackage() {
			return baseline.basePackage();
		}
	}

	private static BaselineLayout.Builder copyBaseline(BaselineLayout source) {
		return source.toBuilder();
	}
}
