package io.github.psm8.archunit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;

import java.util.ArrayList;
import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Level 1 architecture rules for infrastructure-centric applications.
 */
public final class BaselineArchitectureRules {
	private BaselineArchitectureRules() {
	}

	public static ArchRule baseline(String basePackage) {
		return baseline(BaselineLayout.of(basePackage));
	}

	public static ArchRule baseline(BaselineLayout layout) {
		ArchitectureRuleSupport.requireLayout(layout);
		return ArchitectureRuleSupport.combine(List.of(
						cycleRules(layout),
						dependencyBans(layout),
						configurationRules(layout),
						beanRules(layout),
						outputShape(layout)))
				.as("the baseline architecture under " + layout.basePackage())
				.because("baseline rules protect technical boundaries and package cycles");
	}

	private static ArchRule cycleRules(BaselineLayout layout) {
		List<ArchRule> rules = new ArrayList<>();
		for (String pattern : layout.cyclePatterns()) {
			var rule = slices().matching(pattern)
					.should().beFreeOfCycles()
					.as("packages matching " + pattern + " are free of dependency cycles")
					.because("package boundaries remain cycle-free");
			for (BaselineLayout.PatternPair ignored : layout.cycleDependencyIgnores()) {
				rule = rule.ignoreDependency(
						ArchitectureRuleSupport.classPattern(ignored.source()),
						ArchitectureRuleSupport.classPattern(ignored.target()));
			}
			rules.add(rule);
		}
		return ArchitectureRuleSupport.combine(rules);
	}

	private static ArchRule dependencyBans(BaselineLayout layout) {
		List<ArchRule> rules = new ArrayList<>();
		for (BaselineLayout.DependencyBan ban : layout.dependencyBans()) {
			rules.add(classes().that()
					.resideInAnyPackage(ban.sourcePackages().toArray(String[]::new))
					.should(ArchitectureRuleSupport.haveNoDependenciesOn(
							ban.bannedPackages(),
							ban.ignoredDependencies()))
					.as("classes under " + ban.sourcePackages()
							+ " do not depend on " + ban.bannedPackages())
					.allowEmptyShould(true));
		}
		return ArchitectureRuleSupport.combine(rules);
	}

	private static ArchRule configurationRules(BaselineLayout layout) {
		return CompositeArchRule.of(configurationVisibility(layout))
				.and(configurationPropertiesVisibility(layout))
				.and(configurationProxyBeanMethods(layout));
	}

	private static ArchRule configurationVisibility(BaselineLayout layout) {
		return classes().that().areAnnotatedWith(ArchitectureRuleSupport.CONFIGURATION)
				.and(DescribedPredicate.not(
						ArchitectureRuleSupport.classPattern(
								layout.publicConfigurationClasses())))
				.should().bePackagePrivate()
				.as("internal configuration classes are package-private")
				.allowEmptyShould(true);
	}

	private static ArchRule configurationPropertiesVisibility(BaselineLayout layout) {
		return classes().that()
				.areAnnotatedWith(ArchitectureRuleSupport.CONFIGURATION_PROPERTIES)
				.and(DescribedPredicate.not(
						ArchitectureRuleSupport.classPattern(
								layout.publicConfigurationProperties())))
				.should().bePackagePrivate()
				.as("internal configuration properties are package-private")
				.allowEmptyShould(true);
	}

	private static ArchRule configurationProxyBeanMethods(BaselineLayout layout) {
		return classes().that().areAnnotatedWith(ArchitectureRuleSupport.CONFIGURATION)
				.should(ArchitectureRuleSupport.useLiteConfigurationMode())
				.as("configuration uses proxyBeanMethods = false")
				.allowEmptyShould(true);
	}

	private static ArchRule beanRules(BaselineLayout layout) {
		return CompositeArchRule.of(methods()
						.that().areAnnotatedWith(ArchitectureRuleSupport.BEAN)
						.should().beDeclaredInClassesThat()
						.areAnnotatedWith(ArchitectureRuleSupport.CONFIGURATION)
						.as("@Bean methods belong in explicit configuration")
						.allowEmptyShould(true))
				.and(methods()
						.that().areAnnotatedWith(ArchitectureRuleSupport.BEAN)
						.should(ArchitectureRuleSupport.haveConcreteBeanReturnTypes(
								layout.interfaceBeanReturnTypes()))
						.as("@Bean methods expose a concrete type unless a contract is documented")
						.allowEmptyShould(true));
	}

	private static ArchRule outputShape(BaselineLayout layout) {
		if (layout.outputs().isEmpty()
				|| !ArchitectureRuleSupport.present(layout.outputSuffix())) {
			return ArchitectureRuleSupport.emptyRule();
		}
		return classes().that()
				.resideInAnyPackage(layout.outputs().toArray(String[]::new))
				.and().haveSimpleNameEndingWith(layout.outputSuffix())
				.should(ArchitectureRuleSupport.haveImmutableBoundaryShape())
				.as("boundary outputs are records or sealed interfaces")
				.allowEmptyShould(true);
	}
}
