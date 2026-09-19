package io.github.psm8.archunit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;

import java.util.ArrayList;
import java.util.List;

import static com.tngtech.archunit.library.Architectures.onionArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Level 3 architecture rules for dependency-inverted domain applications.
 *
 * <p>Spring and Jakarta annotations are matched by name. This library has no
 * Spring dependency.</p>
 */
public final class HexagonalArchitectureRules {
	private HexagonalArchitectureRules() {
	}

	public static ArchRule hexagonal(String basePackage) {
		return hexagonal(PackageLayout.of(basePackage));
	}

	public static ArchRule strictHexagonal(String basePackage) {
		return hexagonal(PackageLayout.of(basePackage));
	}

	public static ArchRule laxHexagonal(String basePackage) {
		PackageLayout layout = PackageLayout.builder(basePackage)
				.inboundAdapterPackages()
				.outboundAdapterPackages()
				.mixedAdapterPackages(basePackage + ".adapter..")
				.build();
		return hexagonal(layout);
	}

	public static ArchRule hexagonal(PackageLayout layout) {
		ArchitectureRuleSupport.requireLayout(layout);
		return ArchitectureRuleSupport.combine(List.of(
						DomainOrientedArchitectureRules.domainOriented(layout),
						onionRule(layout),
						noFrameworkDependencies(layout),
						portRules(layout),
						outboundAdaptersImplementPorts(layout),
						adapterRules(layout),
						adapterContainment(layout),
						componentScanRules(layout)))
				.as("the hexagonal architecture under " + layout.basePackage())
				.because("dependencies point inward from adapters to application to domain");
	}

	private static ArchRule onionRule(PackageLayout layout) {
		var rule = onionArchitecture().withOptionalLayers(true);
		if (!layout.domain().isEmpty()) {
			rule.domainModels(layout.domain().toArray(String[]::new));
		}
		if (!layout.applicationPackages().isEmpty()) {
			rule.applicationServices(layout.applicationPackages().toArray(String[]::new));
		}
		if (!layout.inboundAdapterPackages().isEmpty()) {
			rule.adapter("in", layout.inboundAdapterPackages().toArray(String[]::new));
		}
		if (!layout.outboundAdapterPackages().isEmpty()) {
			rule.adapter("out", layout.outboundAdapterPackages().toArray(String[]::new));
		}
		if (!layout.mixedAdapterPackages().isEmpty()) {
			rule.adapter("mixed", layout.mixedAdapterPackages().toArray(String[]::new));
		}
		for (PackageLayout.PatternPair ignored : layout.dependencyDirectionIgnores()) {
			rule = rule.ignoreDependency(
					ArchitectureRuleSupport.classPattern(ignored.source()),
					ArchitectureRuleSupport.classPattern(ignored.target()));
		}
		return rule;
	}

	private static ArchRule noFrameworkDependencies(PackageLayout layout) {
		List<String> sources = new ArrayList<>();
		sources.addAll(layout.domain());
		sources.addAll(layout.applicationPackages());
		if (sources.isEmpty() || layout.frameworkTransportPackages().isEmpty()) {
			return ArchitectureRuleSupport.emptyRule();
		}
		return classes().that()
				.resideInAnyPackage(sources.toArray(String[]::new))
				.should(ArchitectureRuleSupport.haveNoFrameworkDependenciesExceptCompositionRoots(
						layout.frameworkTransportPackages(),
						layout.domainModelFrameworkPackages(),
						layout.dependencyDirectionIgnores()))
				.as("domain and application classes have no framework dependencies"
						+ " except explicit composition roots")
				.allowEmptyShould(true);
	}

	private static ArchRule portRules(PackageLayout layout) {
		return CompositeArchRule.of(portPackagesContainInterfaces(layout))
				.and(inboundPortNaming(layout))
				.and(outboundPortNaming(layout))
				.and(portPublic(layout))
				.and(frameworkFreePortSignatures(layout));
	}

	private static ArchRule portPackagesContainInterfaces(PackageLayout layout) {
		String[] inbound = layout.inboundPortPackages().toArray(String[]::new);
		String[] outbound = layout.outboundPortPackages().toArray(String[]::new);
		List<ArchRule> rules = new ArrayList<>();
		if (inbound.length > 0) {
			rules.add(classes().that().resideInAnyPackage(inbound)
					.and().haveSimpleNameEndingWith(layout.useCaseSuffix())
					.should().beInterfaces()
					.as("classes in inbound port packages are interfaces")
					.allowEmptyShould(true));
		}
		if (outbound.length > 0) {
			rules.add(classes().that().resideInAnyPackage(outbound)
					.and().haveSimpleNameEndingWith(layout.portSuffix())
					.should().beInterfaces()
					.as("classes in outbound port packages are interfaces")
					.allowEmptyShould(true));
		}
		return ArchitectureRuleSupport.combine(rules);
	}

	private static ArchRule inboundPortNaming(PackageLayout layout) {
		if (layout.inboundPortPackages().isEmpty()) {
			return ArchitectureRuleSupport.emptyRule();
		}
		return classes().that().resideInAnyPackage(
						layout.inboundPortPackages().toArray(String[]::new))
				.and(ArchitectureRuleSupport.nonSealedPortInterface())
				.should().haveSimpleNameEndingWith(layout.useCaseSuffix())
				.as("inbound ports end with " + layout.useCaseSuffix())
				.allowEmptyShould(true);
	}

	private static ArchRule outboundPortNaming(PackageLayout layout) {
		if (layout.outboundPortPackages().isEmpty()) {
			return ArchitectureRuleSupport.emptyRule();
		}
		return classes().that().resideInAnyPackage(
						layout.outboundPortPackages().toArray(String[]::new))
				.and(ArchitectureRuleSupport.nonSealedPortInterface())
				.should().haveSimpleNameEndingWith(layout.portSuffix())
				.as("outbound ports end with " + layout.portSuffix())
				.allowEmptyShould(true);
	}

	private static ArchRule portPublic(PackageLayout layout) {
		return classes().that(portPredicate(layout))
				.should().bePublic()
				.as("ports are public contracts")
				.allowEmptyShould(true);
	}

	private static ArchRule frameworkFreePortSignatures(PackageLayout layout) {
		String[] ports = ArchitectureRuleSupport.portPackages(layout);
		if (ports.length == 0 || layout.frameworkTransportPackages().isEmpty()) {
			return ArchitectureRuleSupport.emptyRule();
		}
		return classes().that(portPredicate(layout))
				.should(ArchitectureRuleSupport.haveFrameworkFreeSignatures(
						layout.frameworkTransportPackages(),
						ArchitectureRuleSupport.adapterPackages(layout),
						layout.portSignatureExceptions()))
				.as("port signatures are framework and adapter free")
				.allowEmptyShould(true);
	}

	private static ArchRule outboundAdaptersImplementPorts(PackageLayout layout) {
		if (layout.outboundAdapterPackages().isEmpty()
				|| layout.outboundPortPackages().isEmpty()) {
			return ArchitectureRuleSupport.emptyRule();
		}
		return classes().that().resideInAnyPackage(
						layout.outboundAdapterPackages().toArray(String[]::new))
				.and().haveSimpleNameEndingWith(layout.adapterSuffix())
				.should(ArchitectureRuleSupport.implementAnOutboundPort(
						layout.outboundPortPackages(), layout.portSuffix()))
				.as("outbound adapters implement an outbound port")
				.allowEmptyShould(true);
	}

	private static ArchRule adapterRules(PackageLayout layout) {
		String[] adapters = ArchitectureRuleSupport.adapterPackages(layout);
		if (adapters.length == 0) {
			return ArchitectureRuleSupport.emptyRule();
		}
		return classes().that().resideInAnyPackage(adapters)
				.and().haveSimpleNameEndingWith(layout.adapterSuffix())
				.and(DescribedPredicate.not(
						ArchitectureRuleSupport.classPattern(layout.nonAdapterClasses())))
				.should().bePackagePrivate()
				.as("adapter implementations are package-private")
				.allowEmptyShould(true);
	}

	private static ArchRule adapterContainment(PackageLayout layout) {
		String adapterRoot = layout.basePackage() + ".adapter..";
		String[] configured = ArchitectureRuleSupport.adapterPackages(layout);
		if (configured.length == 0) {
			return ArchitectureRuleSupport.emptyRule();
		}
		return classes().that()
				.resideInAnyPackage(adapterRoot)
				.and().haveSimpleNameEndingWith(layout.adapterSuffix())
				.should().resideInAnyPackage(configured)
				.as("adapter implementations are in configured adapter package groups")
				.allowEmptyShould(true);
	}

	private static ArchRule componentScanRules(PackageLayout layout) {
		if (layout.componentScanPackages().isEmpty()) {
			return ArchitectureRuleSupport.emptyRule();
		}
		return classes().that().resideInAnyPackage(
						layout.componentScanPackages().toArray(String[]::new))
				.and(DescribedPredicate.not(
						ArchitectureRuleSupport.classPattern(
								layout.componentScanExceptions())))
				.should(ArchitectureRuleSupport.notBeComponentAnnotated())
				.as("component scanning is absent from configured package groups"
						+ " except documented classes")
				.allowEmptyShould(true);
	}

	private static DescribedPredicate<com.tngtech.archunit.core.domain.JavaClass> portPredicate(
			PackageLayout layout) {
		return ArchitectureRuleSupport.configuredPortPredicate(
						layout.inboundPortPackages(), layout.useCaseSuffix())
				.or(ArchitectureRuleSupport.configuredPortPredicate(
						layout.outboundPortPackages(), layout.portSuffix()));
	}
}
