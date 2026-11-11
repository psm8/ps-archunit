package io.github.psm8.archunit;

import com.tngtech.archunit.lang.ArchRule;

import java.util.ArrayList;
import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Level 2 architecture rules for domain-oriented applications.
 */
public final class DomainOrientedArchitectureRules {
	private DomainOrientedArchitectureRules() {
	}

	public static ArchRule domainOriented(String basePackage) {
		return domainOriented(DomainOrientedLayout.of(basePackage));
	}

	public static ArchRule domainOriented(DomainOrientedLayout layout) {
		ArchitectureRuleSupport.requireLayout(layout);
		return domainOriented(
				layout,
				layout.applicationPackages(),
				layout.apiPackages(),
				layout.infrastructurePackages());
	}

	static ArchRule domainOriented(
			DomainOrientedLayout layout,
			List<String> effectiveApplicationPackages,
			List<String> effectiveApiPackages,
			List<String> effectiveInfrastructurePackages) {
		return ArchitectureRuleSupport.combine(List.of(
						BaselineArchitectureRules.baseline(layout.baseline()),
						classificationRules(
								layout,
								effectiveApplicationPackages,
								effectiveApiPackages,
								effectiveInfrastructurePackages),
						domainDependencyRules(
								layout,
								effectiveApplicationPackages,
								effectiveApiPackages,
								effectiveInfrastructurePackages),
						domainModelFrameworkRules(layout),
						transactionPlacementRules(layout),
						SwitchableRuleSupport.select(layout.defaultMethodMode(), RuleMode.FAIL,
								() -> SwitchableRuleSupport.defaultMethodRule(
										layout, effectiveApplicationPackages))))
				.as("the domain-oriented architecture under " + layout.basePackage())
				.because("domain and application boundaries protect business behavior");
	}

	public static ArchRule domainOrientedAdvisories(String basePackage) {
		return domainOrientedAdvisories(DomainOrientedLayout.of(basePackage));
	}

	/**
	 * Returns the cumulative Level 1–2 switchable rules configured in advisory
	 * mode. Their findings never fail {@link #domainOriented}.
	 */
	public static ArchRule domainOrientedAdvisories(DomainOrientedLayout layout) {
		ArchitectureRuleSupport.requireLayout(layout);
		return domainOrientedAdvisories(layout, layout.applicationPackages())
				.as("the domain-oriented architecture advisories under " + layout.basePackage());
	}

	static ArchRule domainOrientedAdvisories(
			DomainOrientedLayout layout,
			List<String> effectiveApplicationPackages) {
		return ArchitectureRuleSupport.combine(List.of(
				BaselineArchitectureRules.switchableRules(layout.baseline(), RuleMode.ADVISORY),
				SwitchableRuleSupport.select(layout.defaultMethodMode(), RuleMode.ADVISORY,
						() -> SwitchableRuleSupport.defaultMethodRule(
								layout, effectiveApplicationPackages))));
	}

	private static ArchRule classificationRules(
			DomainOrientedLayout layout,
			List<String> applicationPackages,
			List<String> apiPackages,
			List<String> infrastructurePackages) {
		List<String> configuredPackages = new ArrayList<>();
		configuredPackages.addAll(layout.domain());
		configuredPackages.addAll(applicationPackages);
		configuredPackages.addAll(apiPackages);
		configuredPackages.addAll(infrastructurePackages);
		configuredPackages.addAll(layout.sharedLibPackages());
		return ArchitectureRuleSupport.classesBelongToConfiguredPackages(
				layout.basePackage(),
				configuredPackages);
	}

	private static ArchRule domainDependencyRules(
			DomainOrientedLayout layout,
			List<String> applicationPackages,
			List<String> apiPackages,
			List<String> infrastructurePackages) {
		List<ArchRule> rules = new ArrayList<>();
		List<String> domainOutside = new ArrayList<>();
		domainOutside.addAll(applicationPackages);
		domainOutside.addAll(apiPackages);
		domainOutside.addAll(infrastructurePackages);
		domainOutside.addAll(layout.sharedLibPackages());
		addDependencyDirectionRule(
				rules,
				layout.domain(),
				domainOutside,
				layout,
				"domain classes do not depend on application, API, infrastructure, or shared library");

		List<String> applicationOutside = new ArrayList<>();
		applicationOutside.addAll(apiPackages);
		applicationOutside.addAll(infrastructurePackages);
		applicationOutside.addAll(layout.sharedLibPackages());
		addDependencyDirectionRule(
				rules,
				applicationPackages,
				applicationOutside,
				layout,
				"application classes do not depend on API, infrastructure, or shared library");

		List<String> apiOutside = new ArrayList<>();
		apiOutside.addAll(infrastructurePackages);
		addDependencyDirectionRule(
				rules,
				apiPackages,
				apiOutside,
				layout,
				"API classes depend on application or domain, not infrastructure");

		addDependencyDirectionRule(
				rules,
				infrastructurePackages,
				apiPackages,
				layout,
				"infrastructure classes do not depend on API");

		if (!layout.sharedLibPackages().isEmpty()) {
			rules.add(classes().that()
					.resideInAnyPackage(layout.sharedLibPackages().toArray(String[]::new))
					.should(ArchitectureRuleSupport.haveNoDependenciesOnExceptCompositionRoots(
							List.of(layout.basePackage() + ".."),
							layout.sharedLibPackages(),
							layout.dependencyDirectionIgnores()))
					.as("shared library classes depend on nothing else in the base package")
					.allowEmptyShould(true));
		}
		return ArchitectureRuleSupport.combine(rules);
	}

	private static void addDependencyDirectionRule(
			List<ArchRule> rules,
			List<String> sourcePackages,
			List<String> bannedPackages,
			DomainOrientedLayout layout,
			String description) {
		if (sourcePackages.isEmpty() || bannedPackages.isEmpty()) {
			return;
		}
		rules.add(classes().that()
				.resideInAnyPackage(sourcePackages.toArray(String[]::new))
				.should(ArchitectureRuleSupport.haveNoDependenciesOnExceptCompositionRoots(
						bannedPackages,
						layout.dependencyDirectionIgnores()))
				.as(description)
				.allowEmptyShould(true));
	}

	private static ArchRule domainModelFrameworkRules(DomainOrientedLayout layout) {
		if (layout.domain().isEmpty()
				|| layout.frameworkDependencyPackages().isEmpty()) {
			return ArchitectureRuleSupport.emptyRule();
		}
		return classes().that()
				.resideInAnyPackage(layout.domain().toArray(String[]::new))
				.should(ArchitectureRuleSupport.haveNoNonModelFrameworkDependencies(
						layout.frameworkDependencyPackages(),
						layout.domainModelFrameworkPackages(),
						layout.dependencyDirectionIgnores()))
				.as("domain classes use only model framework annotations")
				.allowEmptyShould(true);
	}

	private static ArchRule transactionPlacementRules(DomainOrientedLayout layout) {
		if (layout.transactionAnnotation() == null) {
			return ArchitectureRuleSupport.emptyRule();
		}
		return classes().that()
				.resideInAnyPackage(layout.basePackage() + "..")
				.should(ArchitectureRuleSupport.haveConfiguredAnnotationOnlyInPackages(
						layout.transactionAnnotation(),
						layout.transactionPackages()))
				.as("transaction annotations are limited to configured packages")
				.allowEmptyShould(true);
	}
}
