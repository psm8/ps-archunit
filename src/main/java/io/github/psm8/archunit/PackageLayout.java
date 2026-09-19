package io.github.psm8.archunit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.lang.model.SourceVersion;

/**
 * Immutable package vocabulary and architecture-rule configuration.
 *
 * <p>The builder is mutable; each build creates an immutable snapshot.</p>
 */
public final class PackageLayout {
	private final String basePackage;
	private final List<String> domain;
	private final List<String> applicationPackages;
	private final List<String> inboundPortPackages;
	private final List<String> outboundPortPackages;
	private final List<String> inboundAdapterPackages;
	private final List<String> outboundAdapterPackages;
	private final List<String> mixedAdapterPackages;
	private final List<String> apiPackages;
	private final List<String> infrastructurePackages;
	private final List<String> domainModelFrameworkPackages;
	private final List<String> outputs;
	private final List<DependencyBan> dependencyBans;
	private final List<String> frameworkTransportPackages;
	private final List<String> cyclePatterns;
	private final List<PatternPair> dependencyDirectionIgnores;
	private final List<PatternPair> cycleDependencyIgnores;
	private final List<String> publicConfigurationClasses;
	private final List<String> publicConfigurationProperties;
	private final List<String> interfaceBeanReturnTypes;
	private final List<String> portSignatureExceptions;
	private final List<String> nonAdapterClasses;
	private final List<String> componentScanPackages;
	private final List<String> componentScanExceptions;
	private final String useCaseSuffix;
	private final String portSuffix;
	private final String adapterSuffix;
	private final String outputSuffix;

	private PackageLayout(Builder builder) {
		basePackage = builder.basePackage;
		domain = List.copyOf(builder.domain);
		applicationPackages = List.copyOf(builder.applicationPackages);
		inboundPortPackages = List.copyOf(builder.inboundPortPackages);
		outboundPortPackages = List.copyOf(builder.outboundPortPackages);
		inboundAdapterPackages = List.copyOf(builder.inboundAdapterPackages);
		outboundAdapterPackages = List.copyOf(builder.outboundAdapterPackages);
		mixedAdapterPackages = List.copyOf(builder.mixedAdapterPackages);
		apiPackages = List.copyOf(builder.apiPackages);
		infrastructurePackages = List.copyOf(builder.infrastructurePackages);
		domainModelFrameworkPackages = List.copyOf(builder.domainModelFrameworkPackages);
		outputs = List.copyOf(builder.outputs);
		dependencyBans = List.copyOf(builder.dependencyBans);
		frameworkTransportPackages = List.copyOf(builder.frameworkTransportPackages);
		cyclePatterns = List.copyOf(builder.cyclePatterns);
		dependencyDirectionIgnores = List.copyOf(builder.dependencyDirectionIgnores);
		cycleDependencyIgnores = List.copyOf(builder.cycleDependencyIgnores);
		publicConfigurationClasses = List.copyOf(builder.publicConfigurationClasses);
		publicConfigurationProperties = List.copyOf(builder.publicConfigurationProperties);
		interfaceBeanReturnTypes = List.copyOf(builder.interfaceBeanReturnTypes);
		portSignatureExceptions = List.copyOf(builder.portSignatureExceptions);
		nonAdapterClasses = List.copyOf(builder.nonAdapterClasses);
		componentScanPackages = List.copyOf(builder.componentScanPackages);
		componentScanExceptions = List.copyOf(builder.componentScanExceptions);
		useCaseSuffix = builder.useCaseSuffix;
		portSuffix = builder.portSuffix;
		adapterSuffix = builder.adapterSuffix;
		outputSuffix = builder.outputSuffix;
	}

	public static Builder builder(String basePackage) {
		return new Builder(basePackage);
	}

	public static Builder builder() {
		return new Builder(null);
	}

	public static PackageLayout of(String basePackage) {
		return builder(basePackage).build();
	}

	public Builder toBuilder() {
		Builder copy = new Builder(basePackage);
		copy.domain.addAll(domain);
		copy.applicationPackages.addAll(applicationPackages);
		copy.inboundPortPackages.addAll(inboundPortPackages);
		copy.outboundPortPackages.addAll(outboundPortPackages);
		copy.inboundAdapterPackages.addAll(inboundAdapterPackages);
		copy.outboundAdapterPackages.addAll(outboundAdapterPackages);
		copy.mixedAdapterPackages.addAll(mixedAdapterPackages);
		copy.apiPackages.addAll(apiPackages);
		copy.infrastructurePackages.addAll(infrastructurePackages);
		copy.domainModelFrameworkPackages.addAll(domainModelFrameworkPackages);
		copy.outputs.addAll(outputs);
		copy.dependencyBans.addAll(dependencyBans);
		copy.frameworkTransportPackages.addAll(frameworkTransportPackages);
		copy.cyclePatterns.addAll(cyclePatterns);
		copy.dependencyDirectionIgnores.addAll(dependencyDirectionIgnores);
		copy.cycleDependencyIgnores.addAll(cycleDependencyIgnores);
		copy.publicConfigurationClasses.addAll(publicConfigurationClasses);
		copy.publicConfigurationProperties.addAll(publicConfigurationProperties);
		copy.interfaceBeanReturnTypes.addAll(interfaceBeanReturnTypes);
		copy.portSignatureExceptions.addAll(portSignatureExceptions);
		copy.nonAdapterClasses.addAll(nonAdapterClasses);
		copy.componentScanPackages.addAll(componentScanPackages);
		copy.componentScanExceptions.addAll(componentScanExceptions);
		copy.useCaseSuffix = useCaseSuffix;
		copy.portSuffix = portSuffix;
		copy.adapterSuffix = adapterSuffix;
		copy.outputSuffix = outputSuffix;
		copy.domainConfigured = true;
		copy.applicationConfigured = true;
		copy.inboundPortsConfigured = true;
		copy.outboundPortsConfigured = true;
		copy.inboundAdaptersConfigured = true;
		copy.outboundAdaptersConfigured = true;
		copy.apiConfigured = true;
		copy.infrastructureConfigured = true;
		copy.domainModelFrameworkConfigured = true;
		copy.outputsConfigured = true;
		return copy;
	}

	public String basePackage() {
		return basePackage;
	}

	public List<String> domain() {
		return domain;
	}

	public List<String> applicationPackages() {
		return applicationPackages;
	}

	public List<String> inboundPortPackages() {
		return inboundPortPackages;
	}

	public List<String> outboundPortPackages() {
		return outboundPortPackages;
	}

	public List<String> inboundAdapterPackages() {
		return inboundAdapterPackages;
	}

	public List<String> outboundAdapterPackages() {
		return outboundAdapterPackages;
	}

	public List<String> mixedAdapterPackages() {
		return mixedAdapterPackages;
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

	public List<String> outputs() {
		return outputs;
	}

	public List<DependencyBan> dependencyBans() {
		return dependencyBans;
	}

	public List<String> frameworkTransportPackages() {
		return frameworkTransportPackages;
	}

	public List<String> cyclePatterns() {
		return cyclePatterns;
	}

	public List<PatternPair> dependencyDirectionIgnores() {
		return dependencyDirectionIgnores;
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

	public List<String> portSignatureExceptions() {
		return portSignatureExceptions;
	}

	public List<String> nonAdapterClasses() {
		return nonAdapterClasses;
	}

	public List<String> componentScanPackages() {
		return componentScanPackages;
	}

	public List<String> componentScanExceptions() {
		return componentScanExceptions;
	}

	public String useCaseSuffix() {
		return useCaseSuffix;
	}

	public String portSuffix() {
		return portSuffix;
	}

	public String adapterSuffix() {
		return adapterSuffix;
	}

	public String outputSuffix() {
		return outputSuffix;
	}

	public record DependencyBan(
			List<String> sourcePackages,
			List<String> bannedPackages,
			List<PatternPair> ignoredDependencies) {
		public DependencyBan {
			sourcePackages = copyRequiredPatterns(sourcePackages, "source package");
			bannedPackages = copyRequiredPatterns(bannedPackages, "banned package");
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
			source = requiredPattern(source);
			target = requiredPattern(target);
		}
	}

	public static final class Builder {
		private String basePackage;
		private final List<String> domain = new ArrayList<>();
		private final List<String> applicationPackages = new ArrayList<>();
		private final List<String> inboundPortPackages = new ArrayList<>();
		private final List<String> outboundPortPackages = new ArrayList<>();
		private final List<String> inboundAdapterPackages = new ArrayList<>();
		private final List<String> outboundAdapterPackages = new ArrayList<>();
		private final List<String> mixedAdapterPackages = new ArrayList<>();
		private final List<String> apiPackages = new ArrayList<>();
		private final List<String> infrastructurePackages = new ArrayList<>();
		private final List<String> domainModelFrameworkPackages = new ArrayList<>();
		private final List<String> outputs = new ArrayList<>();
		private final List<DependencyBan> dependencyBans = new ArrayList<>();
		private final List<String> frameworkTransportPackages = new ArrayList<>();
		private final List<String> cyclePatterns = new ArrayList<>();
		private final List<PatternPair> dependencyDirectionIgnores = new ArrayList<>();
		private final List<PatternPair> cycleDependencyIgnores = new ArrayList<>();
		private final List<String> publicConfigurationClasses = new ArrayList<>();
		private final List<String> publicConfigurationProperties = new ArrayList<>();
		private final List<String> interfaceBeanReturnTypes = new ArrayList<>();
		private final List<String> portSignatureExceptions = new ArrayList<>();
		private final List<String> nonAdapterClasses = new ArrayList<>();
		private final List<String> componentScanPackages = new ArrayList<>();
		private final List<String> componentScanExceptions = new ArrayList<>();
		private String useCaseSuffix;
		private String portSuffix;
		private String adapterSuffix;
		private String outputSuffix;
		private boolean domainConfigured;
		private boolean applicationConfigured;
		private boolean inboundPortsConfigured;
		private boolean outboundPortsConfigured;
		private boolean inboundAdaptersConfigured;
		private boolean outboundAdaptersConfigured;
		private boolean apiConfigured;
		private boolean infrastructureConfigured;
		private boolean domainModelFrameworkConfigured;
		private boolean outputsConfigured;

		private Builder(String basePackage) {
			this.basePackage = basePackage;
		}

		public Builder basePackage(String value) {
			basePackage = value;
			return this;
		}

		public Builder domain(String... values) {
			domainConfigured = true;
			replacePatterns(domain, values);
			return this;
		}

		public Builder addDomains(String... values) {
			addPatterns(domain, values);
			return this;
		}

		public Builder applicationPackages(String... values) {
			applicationConfigured = true;
			replacePatterns(applicationPackages, values);
			return this;
		}

		public Builder addApplicationPackages(String... values) {
			addPatterns(applicationPackages, values);
			return this;
		}

		public Builder inboundPortPackages(String... values) {
			inboundPortsConfigured = true;
			replacePatterns(inboundPortPackages, values);
			return this;
		}

		public Builder addInboundPortPackages(String... values) {
			addPatterns(inboundPortPackages, values);
			return this;
		}

		public Builder outboundPortPackages(String... values) {
			outboundPortsConfigured = true;
			replacePatterns(outboundPortPackages, values);
			return this;
		}

		public Builder addOutboundPortPackages(String... values) {
			addPatterns(outboundPortPackages, values);
			return this;
		}

		public Builder inboundAdapterPackages(String... values) {
			inboundAdaptersConfigured = true;
			replacePatterns(inboundAdapterPackages, values);
			return this;
		}

		public Builder addInboundAdapterPackages(String... values) {
			addPatterns(inboundAdapterPackages, values);
			return this;
		}

		public Builder outboundAdapterPackages(String... values) {
			outboundAdaptersConfigured = true;
			replacePatterns(outboundAdapterPackages, values);
			return this;
		}

		public Builder addOutboundAdapterPackages(String... values) {
			addPatterns(outboundAdapterPackages, values);
			return this;
		}

		public Builder mixedAdapterPackages(String... values) {
			replacePatterns(mixedAdapterPackages, values);
			return this;
		}

		public Builder addMixedAdapterPackages(String... values) {
			addPatterns(mixedAdapterPackages, values);
			return this;
		}

		public Builder apiPackages(String... values) {
			apiConfigured = true;
			replacePatterns(apiPackages, values);
			return this;
		}

		public Builder addApiPackages(String... values) {
			addPatterns(apiPackages, values);
			return this;
		}

		public Builder infrastructurePackages(String... values) {
			infrastructureConfigured = true;
			replacePatterns(infrastructurePackages, values);
			return this;
		}

		public Builder addInfrastructurePackages(String... values) {
			addPatterns(infrastructurePackages, values);
			return this;
		}

		public Builder domainModelFrameworkPackages(String... values) {
			domainModelFrameworkConfigured = true;
			replacePatterns(domainModelFrameworkPackages, values);
			return this;
		}

		public Builder addDomainModelFrameworkPackages(String... values) {
			addPatterns(domainModelFrameworkPackages, values);
			return this;
		}

		public Builder outputs(String... values) {
			outputsConfigured = true;
			replacePatterns(outputs, values);
			return this;
		}

		public Builder addOutputs(String... values) {
			addPatterns(outputs, values);
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

		public Builder frameworkTransportPackages(String... values) {
			replacePatterns(frameworkTransportPackages, values);
			return this;
		}

		public Builder cyclePatterns(String... values) {
			replacePatterns(cyclePatterns, values);
			return this;
		}

		public Builder ignoreDependency(String source, String target) {
			dependencyDirectionIgnores.add(new PatternPair(source, target));
			return this;
		}

		public Builder ignoreCycleDependency(String source, String target) {
			cycleDependencyIgnores.add(new PatternPair(source, target));
			return this;
		}

		public Builder publicConfigurationClasses(String... values) {
			replacePatterns(publicConfigurationClasses, values);
			return this;
		}

		public Builder publicConfigurationProperties(String... values) {
			replacePatterns(publicConfigurationProperties, values);
			return this;
		}

		public Builder interfaceBeanReturnTypes(String... values) {
			replacePatterns(interfaceBeanReturnTypes, values);
			return this;
		}

		public Builder portSignatureExceptions(String... values) {
			replacePatterns(portSignatureExceptions, values);
			return this;
		}

		public Builder nonAdapterClasses(String... values) {
			replacePatterns(nonAdapterClasses, values);
			return this;
		}

		public Builder componentScanPackages(String... values) {
			replacePatterns(componentScanPackages, values);
			return this;
		}

		public Builder addComponentScanPackages(String... values) {
			addPatterns(componentScanPackages, values);
			return this;
		}

		public Builder componentScanExceptions(String... values) {
			replacePatterns(componentScanExceptions, values);
			return this;
		}

		public Builder addComponentScanExceptions(String... values) {
			addPatterns(componentScanExceptions, values);
			return this;
		}

		public Builder useCaseSuffix(String value) {
			useCaseSuffix = optionalText(value);
			return this;
		}

		public Builder portSuffix(String value) {
			portSuffix = optionalText(value);
			return this;
		}

		public Builder adapterSuffix(String value) {
			adapterSuffix = optionalText(value);
			return this;
		}

		public Builder outputSuffix(String value) {
			outputSuffix = optionalText(value);
			return this;
		}

		public PackageLayout build() {
			validateBasePackage(basePackage);
			applyDefaults();
			return new PackageLayout(this);
		}

		private void applyDefaults() {
			if (!domainConfigured) {
				prependDefaults(domain, basePackage + ".domain..");
			}
			if (!applicationConfigured) {
				prependDefaults(applicationPackages, basePackage + ".application..");
			}
			if (!inboundPortsConfigured) {
				prependDefaults(inboundPortPackages, basePackage + ".application.port.in..");
			}
			if (!outboundPortsConfigured) {
				prependDefaults(
						outboundPortPackages,
						basePackage + ".application.port.out..");
			}
			if (!inboundAdaptersConfigured) {
				prependDefaults(inboundAdapterPackages, basePackage + ".adapter.in..");
			}
			if (!outboundAdaptersConfigured) {
				prependDefaults(outboundAdapterPackages, basePackage + ".adapter.out..");
			}
			if (!apiConfigured) {
				prependDefaults(
						apiPackages,
						mergeDefaults(basePackage + ".api..", inboundAdapterPackages));
			}
			if (!infrastructureConfigured) {
				prependDefaults(
						infrastructurePackages,
						mergeDefaults(
								basePackage + ".infrastructure..",
								outboundAdapterPackages,
								mixedAdapterPackages));
			}
			if (!domainModelFrameworkConfigured) {
				prependDefaults(
						domainModelFrameworkPackages,
						"jakarta.persistence..",
						"jakarta.validation..",
						"javax.persistence..",
						"javax.validation..",
						"com.fasterxml.jackson.annotation..");
			}
			if (!outputsConfigured) {
				prependDefaults(outputs, basePackage + "..");
			}
			useCaseSuffix = defaultText(useCaseSuffix, "UseCase");
			portSuffix = defaultText(portSuffix, "Port");
			adapterSuffix = defaultText(adapterSuffix, "Adapter");
			outputSuffix = defaultText(outputSuffix, "Output");
			if (cyclePatterns.isEmpty()) {
				cyclePatterns.add(basePackage + ".(**)");
			}
			if (frameworkTransportPackages.isEmpty()) {
				frameworkTransportPackages.add("org.springframework..");
				frameworkTransportPackages.add("jakarta..");
				frameworkTransportPackages.add("javax..");
				frameworkTransportPackages.add("com.fasterxml.jackson..");
			}
			for (String modelFrameworkPackage : domainModelFrameworkPackages) {
				if (!frameworkTransportPackages.contains(modelFrameworkPackage)) {
					frameworkTransportPackages.add(modelFrameworkPackage);
				}
			}
		}
	}

	private static String defaultText(String value, String fallback) {
		return value == null ? fallback : value;
	}

	private static void prependDefaults(List<String> target, String... defaults) {
		List<String> additions = new ArrayList<>(target);
		target.clear();
		addPatterns(target, defaults);
		target.addAll(additions);
	}

	private static String[] mergeDefaults(
			String first,
			List<String> second) {
		List<String> merged = new ArrayList<>();
		merged.add(first);
		merged.addAll(second);
		return merged.toArray(String[]::new);
	}

	private static String[] mergeDefaults(
			String first,
			List<String> second,
			List<String> third) {
		List<String> merged = new ArrayList<>();
		merged.add(first);
		merged.addAll(second);
		merged.addAll(third);
		return merged.toArray(String[]::new);
	}

	private static List<String> copyRequiredPatterns(List<String> values, String label) {
		Objects.requireNonNull(values, label + "s");
		List<String> copy = new ArrayList<>();
		for (String value : values) {
			copy.add(requiredPattern(value));
		}
		return List.copyOf(copy);
	}

	private static String requiredPattern(String value) {
		if (value == null || value.isBlank() || !value.equals(value.trim())
				|| value.contains(" ")) {
			throw new IllegalArgumentException(
					"package pattern must not contain whitespace: " + value);
		}
		return value;
	}

	private static String optionalPattern(String value) {
		if (value == null) {
			return null;
		}
		if (value.isBlank()) {
			return "";
		}
		return requiredPattern(value);
	}

	private static String optionalText(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		if (!value.equals(value.trim())) {
			throw new IllegalArgumentException("option value must not be padded: " + value);
		}
		return value;
	}

	private static void replacePatterns(List<String> target, String... values) {
		target.clear();
		addPatterns(target, values);
	}

	private static void addPatterns(List<String> target, String... values) {
		if (values == null) {
			return;
		}
		for (String value : values) {
			if (value == null) {
				continue;
			}
			String pattern = optionalPattern(value);
			if (pattern != null && !pattern.isEmpty()) {
				target.add(pattern);
			}
		}
	}

	private static void validateBasePackage(String value) {
		if (value == null
				|| !value.equals(value.trim())
				|| value.isBlank()
				|| !SourceVersion.isName(value)) {
			throw new IllegalArgumentException(
					"basePackage must be a concrete Java package name: " + value);
		}
	}
}
