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
				layout.apiPackages(),
				layout.infrastructurePackages());
	}

	static ArchRule domainOriented(
			DomainOrientedLayout layout,
			List<String> effectiveApiPackages,
			List<String> effectiveInfrastructurePackages) {
		return ArchitectureRuleSupport.combine(List.of(
						BaselineArchitectureRules.baseline(layout.baseline()),
						domainDependencyRules(
								layout, effectiveApiPackages, effectiveInfrastructurePackages),
						domainModelFrameworkRules(layout)))
				.as("the domain-oriented architecture under " + layout.basePackage())
				.because("domain and application boundaries protect business behavior");
	}

	private static ArchRule domainDependencyRules(
			DomainOrientedLayout layout,
			List<String> apiPackages,
			List<String> infrastructurePackages) {
		List<ArchRule> rules = new ArrayList<>();
		List<String> domainOutside = new ArrayList<>();
		domainOutside.addAll(layout.applicationPackages());
		domainOutside.addAll(apiPackages);
		domainOutside.addAll(infrastructurePackages);
		addDependencyDirectionRule(
				rules,
				layout.domain(),
				domainOutside,
				layout,
				"domain classes do not depend on application, API, or infrastructure");

		List<String> applicationOutside = new ArrayList<>();
		applicationOutside.addAll(apiPackages);
		applicationOutside.addAll(infrastructurePackages);
		addDependencyDirectionRule(
				rules,
				layout.applicationPackages(),
				applicationOutside,
				layout,
				"application classes do not depend on API or infrastructure");

		List<String> apiOutside = new ArrayList<>();
		apiOutside.addAll(layout.domain());
		apiOutside.addAll(infrastructurePackages);
		addDependencyDirectionRule(
				rules,
				apiPackages,
				apiOutside,
				layout,
				"API classes depend inward on application only");

		addDependencyDirectionRule(
				rules,
				infrastructurePackages,
				apiPackages,
				layout,
				"infrastructure classes do not depend on API");
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
				.should(ArchitectureRuleSupport.haveNoDependenciesOn(
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
}
