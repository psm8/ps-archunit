package io.github.psm8.archunit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Immutable cumulative Level 3 package vocabulary and rule configuration.
 */
public final class HexagonalLayout {
	private final DomainOrientedLayout domainOriented;
	private final List<String> inboundPortPackages;
	private final List<String> outboundPortPackages;
	private final List<String> inboundAdapterPackages;
	private final List<String> outboundAdapterPackages;
	private final List<String> mixedAdapterPackages;
	private final List<String> portSignatureExceptions;
	private final List<String> nonAdapterClasses;
	private final String useCaseSuffix;
	private final String portSuffix;
	private final String adapterSuffix;

	private HexagonalLayout(Builder builder) {
		domainOriented = builder.domainOriented.build();
		inboundPortPackages = List.copyOf(builder.inboundPortPackages);
		outboundPortPackages = List.copyOf(builder.outboundPortPackages);
		inboundAdapterPackages = List.copyOf(builder.inboundAdapterPackages);
		outboundAdapterPackages = List.copyOf(builder.outboundAdapterPackages);
		mixedAdapterPackages = List.copyOf(builder.mixedAdapterPackages);
		portSignatureExceptions = List.copyOf(builder.portSignatureExceptions);
		nonAdapterClasses = List.copyOf(builder.nonAdapterClasses);
		useCaseSuffix = builder.useCaseSuffix;
		portSuffix = builder.portSuffix;
		adapterSuffix = builder.adapterSuffix;
	}

	public static Builder builder(String basePackage) {
		return new Builder(DomainOrientedLayout.builder(basePackage));
	}

	public static Builder builder(DomainOrientedLayout domainOriented) {
		Objects.requireNonNull(domainOriented, "domainOriented");
		return new Builder(domainOriented.toBuilder());
	}

	public static Builder builder(BaselineLayout baseline) {
		Objects.requireNonNull(baseline, "baseline");
		return new Builder(DomainOrientedLayout.builder(baseline));
	}

	public static Builder builder() {
		return builder((String) null);
	}

	public static HexagonalLayout of(String basePackage) {
		return builder(basePackage).build();
	}

	public static HexagonalLayout of(DomainOrientedLayout domainOriented) {
		return builder(domainOriented).build();
	}

	public static HexagonalLayout of(BaselineLayout baseline) {
		return builder(baseline).build();
	}

	public Builder toBuilder() {
		Builder copy = builder(domainOriented)
				.inboundPortPackages(inboundPortPackages.toArray(String[]::new))
				.outboundPortPackages(outboundPortPackages.toArray(String[]::new))
				.inboundAdapterPackages(inboundAdapterPackages.toArray(String[]::new))
				.outboundAdapterPackages(outboundAdapterPackages.toArray(String[]::new))
				.mixedAdapterPackages(mixedAdapterPackages.toArray(String[]::new))
				.portSignatureExceptions(portSignatureExceptions.toArray(String[]::new))
				.nonAdapterClasses(nonAdapterClasses.toArray(String[]::new))
				.useCaseSuffix(useCaseSuffix)
				.portSuffix(portSuffix)
				.adapterSuffix(adapterSuffix);
		copy.inboundPortsConfigured = true;
		copy.outboundPortsConfigured = true;
		copy.inboundAdaptersConfigured = true;
		copy.outboundAdaptersConfigured = true;
		return copy;
	}

	DomainOrientedLayout domainOriented() {
		return domainOriented;
	}

	public String basePackage() {
		return domainOriented.basePackage();
	}

	public List<String> domain() {
		return domainOriented.domain();
	}

	public List<String> applicationPackages() {
		return domainOriented.applicationPackages();
	}

	List<String> applicationCorePackages() {
		return combine(applicationPackages(), inboundPortPackages, outboundPortPackages);
	}

	List<String> apiPackages() {
		return domainOriented.apiPackages();
	}

	public List<String> infrastructurePackages() {
		return domainOriented.infrastructurePackages();
	}

	public List<String> domainModelFrameworkPackages() {
		return domainOriented.domainModelFrameworkPackages();
	}

	public List<String> frameworkDependencyPackages() {
		return domainOriented.frameworkDependencyPackages();
	}

	public List<String> outputs() {
		return domainOriented.outputs();
	}

	public List<BaselineLayout.DependencyBan> dependencyBans() {
		return domainOriented.dependencyBans();
	}

	public List<String> cyclePatterns() {
		return domainOriented.cyclePatterns();
	}

	public List<BaselineLayout.PatternPair> dependencyDirectionIgnores() {
		return domainOriented.dependencyDirectionIgnores();
	}

	public List<BaselineLayout.PatternPair> cycleDependencyIgnores() {
		return domainOriented.cycleDependencyIgnores();
	}

	public List<String> publicConfigurationClasses() {
		return domainOriented.publicConfigurationClasses();
	}

	public List<String> publicConfigurationProperties() {
		return domainOriented.publicConfigurationProperties();
	}

	public List<String> interfaceBeanReturnTypes() {
		return domainOriented.interfaceBeanReturnTypes();
	}

	public String outputSuffix() {
		return domainOriented.outputSuffix();
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

	public List<String> portSignatureExceptions() {
		return portSignatureExceptions;
	}

	public List<String> nonAdapterClasses() {
		return nonAdapterClasses;
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

	List<String> effectiveApiPackages() {
		return combine(apiPackages(), inboundAdapterPackages);
	}

	List<String> effectiveInfrastructurePackages() {
		return combine(infrastructurePackages(), outboundAdapterPackages, mixedAdapterPackages);
	}

	private static List<String> combine(List<String> first, List<String> second) {
		return combineGroups(first, List.of(second));
	}

	private static List<String> combine(
			List<String> first, List<String> second, List<String> third) {
		return combineGroups(first, List.of(second, third));
	}

	private static List<String> combineGroups(
			List<String> first, List<List<String>> rest) {
		List<String> result = new ArrayList<>(first);
		for (List<String> group : rest) {
			for (String value : group) {
				if (!result.contains(value)) {
					result.add(value);
				}
			}
		}
		return List.copyOf(result);
	}

	public static final class Builder {
		private final DomainOrientedLayout.Builder domainOriented;
		private final List<String> inboundPortPackages = new ArrayList<>();
		private final List<String> outboundPortPackages = new ArrayList<>();
		private final List<String> inboundAdapterPackages = new ArrayList<>();
		private final List<String> outboundAdapterPackages = new ArrayList<>();
		private final List<String> mixedAdapterPackages = new ArrayList<>();
		private final List<String> portSignatureExceptions = new ArrayList<>();
		private final List<String> nonAdapterClasses = new ArrayList<>();
		private String useCaseSuffix;
		private String portSuffix;
		private String adapterSuffix;
		private boolean inboundPortsConfigured;
		private boolean outboundPortsConfigured;
		private boolean inboundAdaptersConfigured;
		private boolean outboundAdaptersConfigured;

		private Builder(DomainOrientedLayout.Builder domainOriented) {
			this.domainOriented = domainOriented;
		}

		public Builder basePackage(String value) {
			domainOriented.basePackage(value);
			return this;
		}

		public Builder outputs(String... values) {
			domainOriented.outputs(values);
			return this;
		}

		public Builder addOutputs(String... values) {
			domainOriented.addOutputs(values);
			return this;
		}

		public Builder dependencyBans(BaselineLayout.DependencyBan... values) {
			domainOriented.dependencyBans(values);
			return this;
		}

		public Builder cyclePatterns(String... values) {
			domainOriented.cyclePatterns(values);
			return this;
		}

		public Builder ignoreDependency(String source, String target) {
			domainOriented.ignoreDependency(source, target);
			return this;
		}

		public Builder ignoreCycleDependency(String source, String target) {
			domainOriented.ignoreCycleDependency(source, target);
			return this;
		}

		public Builder publicConfigurationClasses(String... values) {
			domainOriented.publicConfigurationClasses(values);
			return this;
		}

		public Builder publicConfigurationProperties(String... values) {
			domainOriented.publicConfigurationProperties(values);
			return this;
		}

		public Builder interfaceBeanReturnTypes(String... values) {
			domainOriented.interfaceBeanReturnTypes(values);
			return this;
		}

		public Builder outputSuffix(String value) {
			domainOriented.outputSuffix(value);
			return this;
		}

		public Builder domain(String... values) {
			domainOriented.domain(values);
			return this;
		}

		public Builder addDomains(String... values) {
			domainOriented.addDomains(values);
			return this;
		}

		public Builder applicationPackages(String... values) {
			domainOriented.applicationPackages(values);
			return this;
		}

		public Builder addApplicationPackages(String... values) {
			domainOriented.addApplicationPackages(values);
			return this;
		}

		Builder apiPackages(String... values) {
			domainOriented.apiPackages(values);
			return this;
		}

		Builder addApiPackages(String... values) {
			domainOriented.addApiPackages(values);
			return this;
		}

		public Builder infrastructurePackages(String... values) {
			domainOriented.infrastructurePackages(values);
			return this;
		}

		public Builder addInfrastructurePackages(String... values) {
			domainOriented.addInfrastructurePackages(values);
			return this;
		}

		public Builder domainModelFrameworkPackages(String... values) {
			domainOriented.domainModelFrameworkPackages(values);
			return this;
		}

		public Builder addDomainModelFrameworkPackages(String... values) {
			domainOriented.addDomainModelFrameworkPackages(values);
			return this;
		}

		public Builder frameworkDependencyPackages(String... values) {
			domainOriented.frameworkDependencyPackages(values);
			return this;
		}

		public Builder addFrameworkDependencyPackages(String... values) {
			domainOriented.addFrameworkDependencyPackages(values);
			return this;
		}

		public Builder inboundPortPackages(String... values) {
			inboundPortsConfigured = true;
			LayoutSupport.replacePatterns(inboundPortPackages, values);
			return this;
		}

		public Builder addInboundPortPackages(String... values) {
			LayoutSupport.addPatterns(inboundPortPackages, values);
			return this;
		}

		public Builder outboundPortPackages(String... values) {
			outboundPortsConfigured = true;
			LayoutSupport.replacePatterns(outboundPortPackages, values);
			return this;
		}

		public Builder addOutboundPortPackages(String... values) {
			LayoutSupport.addPatterns(outboundPortPackages, values);
			return this;
		}

		public Builder inboundAdapterPackages(String... values) {
			inboundAdaptersConfigured = true;
			LayoutSupport.replacePatterns(inboundAdapterPackages, values);
			return this;
		}

		public Builder addInboundAdapterPackages(String... values) {
			LayoutSupport.addPatterns(inboundAdapterPackages, values);
			return this;
		}

		public Builder outboundAdapterPackages(String... values) {
			outboundAdaptersConfigured = true;
			LayoutSupport.replacePatterns(outboundAdapterPackages, values);
			return this;
		}

		public Builder addOutboundAdapterPackages(String... values) {
			LayoutSupport.addPatterns(outboundAdapterPackages, values);
			return this;
		}

		public Builder mixedAdapterPackages(String... values) {
			LayoutSupport.replacePatterns(mixedAdapterPackages, values);
			return this;
		}

		public Builder addMixedAdapterPackages(String... values) {
			LayoutSupport.addPatterns(mixedAdapterPackages, values);
			return this;
		}

		public Builder portSignatureExceptions(String... values) {
			LayoutSupport.replacePatterns(portSignatureExceptions, values);
			return this;
		}

		public Builder nonAdapterClasses(String... values) {
			LayoutSupport.replacePatterns(nonAdapterClasses, values);
			return this;
		}

		public Builder useCaseSuffix(String value) {
			useCaseSuffix = LayoutSupport.optionalText(value);
			return this;
		}

		public Builder portSuffix(String value) {
			portSuffix = LayoutSupport.optionalText(value);
			return this;
		}

		public Builder adapterSuffix(String value) {
			adapterSuffix = LayoutSupport.optionalText(value);
			return this;
		}

		public HexagonalLayout build() {
			if (!inboundPortsConfigured) {
				LayoutSupport.prependPatterns(
						inboundPortPackages, basePackage() + ".application.port.in..");
			}
			if (!outboundPortsConfigured) {
				LayoutSupport.prependPatterns(
						outboundPortPackages, basePackage() + ".application.port.out..");
			}
			if (!inboundAdaptersConfigured) {
				LayoutSupport.prependPatterns(
						inboundAdapterPackages, basePackage() + ".adapter.in..");
			}
			if (!outboundAdaptersConfigured) {
				LayoutSupport.prependPatterns(
						outboundAdapterPackages, basePackage() + ".adapter.out..");
			}
			useCaseSuffix = LayoutSupport.defaultText(useCaseSuffix, "UseCase");
			portSuffix = LayoutSupport.defaultText(portSuffix, "Port");
			adapterSuffix = LayoutSupport.defaultText(adapterSuffix, "Adapter");
			return new HexagonalLayout(this);
		}

		private String basePackage() {
			return domainOriented.basePackage();
		}
	}
}
