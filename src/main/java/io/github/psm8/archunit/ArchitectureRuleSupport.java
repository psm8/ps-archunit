package io.github.psm8.archunit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

final class ArchitectureRuleSupport {
	static final String CONFIGURATION =
			"org.springframework.context.annotation.Configuration";
	static final String BEAN =
			"org.springframework.context.annotation.Bean";
	static final String CONFIGURATION_PROPERTIES =
			"org.springframework.boot.context.properties.ConfigurationProperties";
	static final String NO_OPTIONAL_RULES =
			"io.github.psm8.archunit.internal.NoSuchArchitectureAnnotation";

	private ArchitectureRuleSupport() {
	}

	static void requireLayout(BaselineLayout layout) {
		requireNonNullLayout(layout);
	}

	static void requireLayout(DomainOrientedLayout layout) {
		requireNonNullLayout(layout);
	}

	static void requireLayout(HexagonalLayout layout) {
		requireNonNullLayout(layout);
	}

	private static void requireNonNullLayout(Object layout) {
		if (layout == null) {
			throw new IllegalArgumentException("layout must not be null");
		}
	}

	static ArchCondition<JavaClass> haveNoDependenciesOn(
			List<String> banned,
			List<BaselineLayout.PatternPair> exceptions) {
		return new ArchCondition<>("not depend on banned packages") {
			@Override
			public void check(JavaClass source, ConditionEvents events) {
				for (Dependency dependency : source.getDirectDependenciesFromSelf()) {
					JavaClass target = dependency.getTargetClass();
					if (isInAnyPackage(target, banned) && !isIgnored(source, target, exceptions)) {
						events.add(SimpleConditionEvent.violated(source,
								dependency.getDescription()));
					}
				}
			}
		};
	}

	static ArchRule classesBelongToConfiguredPackages(
			String basePackage,
			List<String> configuredPackages) {
		return classes().that()
				.resideInAnyPackage(basePackage + "..")
				.should(belongToConfiguredPackages(configuredPackages))
				.as("classes under " + basePackage
						+ " belong to an architecture package group")
				.allowEmptyShould(true);
	}

	private static ArchCondition<JavaClass> belongToConfiguredPackages(
			List<String> configuredPackages) {
		return new ArchCondition<>("belong to one of " + configuredPackages) {
			@Override
			public void check(JavaClass item, ConditionEvents events) {
				boolean configured = configuredPackages.stream()
						.anyMatch(pattern -> matches(item.getPackageName(), pattern));
				if (!configured) {
					events.add(SimpleConditionEvent.violated(item,
							item.getName() + " does not belong to an architecture package group"));
				}
			}
		};
	}

	static ArchCondition<JavaClass> haveNoNonModelFrameworkDependencies(
			List<String> frameworkPackages,
			List<String> modelFrameworkPackages,
			List<BaselineLayout.PatternPair> exceptions) {
		return new ArchCondition<>("not depend on non-model framework packages") {
			@Override
			public void check(JavaClass source, ConditionEvents events) {
				for (Dependency dependency : source.getDirectDependenciesFromSelf()) {
					JavaClass target = dependency.getTargetClass();
					boolean frameworkDependency = isInAnyPackage(target, frameworkPackages);
					boolean modelAnnotation = isModelAnnotationDependency(
							source, target, modelFrameworkPackages);
					if (frameworkDependency
							&& !modelAnnotation
							&& !isIgnored(source, target, exceptions)) {
						events.add(SimpleConditionEvent.violated(source,
								dependency.getDescription()));
					}
				}
			}
		};
	}

	static ArchCondition<JavaClass> haveNoFrameworkDependenciesExceptCompositionRoots(
			List<String> frameworkPackages,
			List<String> modelFrameworkPackages,
			List<BaselineLayout.PatternPair> exceptions) {
		return new ArchCondition<>("not depend on framework packages outside composition roots") {
			@Override
			public void check(JavaClass source, ConditionEvents events) {
				if (isCompositionRoot(source)) {
					return;
				}
				for (Dependency dependency : source.getDirectDependenciesFromSelf()) {
					JavaClass target = dependency.getTargetClass();
					if (isInAnyPackage(target, frameworkPackages)
							&& !isModelAnnotationDependency(
							source, target, modelFrameworkPackages)
							&& !isIgnored(source, target, exceptions)) {
						events.add(SimpleConditionEvent.violated(source,
								dependency.getDescription()));
					}
				}
			}
		};
	}

	static ArchCondition<JavaClass> implementAnOutboundPort(
			List<String> packages,
			String suffix) {
		return new ArchCondition<>("implement an interface under " + packages) {
			@Override
			public void check(JavaClass item, ConditionEvents events) {
				boolean implementsPort = item.getAllRawInterfaces().stream()
						.anyMatch(type -> isNonSealedInterface(type)
								&& type.getSimpleName().endsWith(suffix)
								&& packages.stream().anyMatch(
								pattern -> matches(type.getPackageName(), pattern)));
				if (!implementsPort) {
					events.add(SimpleConditionEvent.violated(item,
							item.getName() + " does not implement an outbound port"));
				}
			}
		};
	}

	static ArchCondition<JavaClass> haveFrameworkFreeSignatures(
			List<String> frameworkPackages,
			String[] adapterPackages,
			List<String> exceptions) {
		return new ArchCondition<>("have framework-free port signatures") {
			@Override
			public void check(JavaClass port, ConditionEvents events) {
				if (matchesAny(port.getName(), exceptions)) {
					return;
				}
				for (JavaMethod method : port.getMethods()) {
					for (JavaClass type : method.getAllInvolvedRawTypes()) {
						if (isInAnyPackage(type, frameworkPackages, adapterPackages)) {
							events.add(SimpleConditionEvent.violated(method,
									method.getFullName() + " exposes " + type.getName()));
						}
					}
					for (JavaAnnotation<? extends JavaMethod> annotation : method.getAnnotations()) {
						if (isInAnyPackage(annotation.getRawType(), frameworkPackages, adapterPackages)) {
							events.add(SimpleConditionEvent.violated(method,
									method.getFullName() + " uses "
											+ annotation.getRawType().getName()));
						}
					}
				}
			}
		};
	}

	static ArchCondition<JavaClass> useLiteConfigurationMode() {
		return new ArchCondition<>("use proxyBeanMethods = false") {
			@Override
			public void check(JavaClass configuration, ConditionEvents events) {
				JavaAnnotation<?> annotation = configuration.getAnnotationOfType(CONFIGURATION);
				Object proxyBeanMethods = annotation.get("proxyBeanMethods").orElse(Boolean.TRUE);
				if (Boolean.TRUE.equals(proxyBeanMethods)) {
					events.add(SimpleConditionEvent.violated(configuration,
							configuration.getName() + " enables inter-bean proxying"));
				}
			}
		};
	}

	static ArchCondition<JavaMethod> haveValidBeanExposure(
			String basePackage,
			List<String> allowedApplicationInterfaceTypes) {
		return new ArchCondition<>(
				"return a concrete type, an external interface, or an allowed application interface") {
			@Override
			public void check(JavaMethod method, ConditionEvents events) {
				JavaClass returnType = method.getRawReturnType();
				boolean applicationInterface = returnType.isInterface()
						&& isInBasePackage(returnType.getPackageName(), basePackage);
				if (applicationInterface
						&& !allowedApplicationInterfaceTypes.contains(returnType.getName())) {
					events.add(SimpleConditionEvent.violated(method,
							method.getFullName() + " returns interface "
									+ returnType.getName()
									+ " owned by the application"));
				}
			}
		};
	}

	static boolean isInBasePackage(String packageName, String basePackage) {
		return packageName.equals(basePackage)
				|| packageName.startsWith(basePackage + ".");
	}

	static ArchCondition<JavaClass> haveImmutableBoundaryShape() {
		return new ArchCondition<>("be a record or sealed interface") {
			@Override
			public void check(JavaClass boundaryType, ConditionEvents events) {
				boolean sealedInterface = boundaryType.isInterface()
						&& boundaryType.reflect().isSealed();
				if (!boundaryType.isRecord() && !sealedInterface) {
					events.add(SimpleConditionEvent.violated(boundaryType,
							boundaryType.getName()
									+ " is neither a record nor a sealed interface"));
				}
			}
		};
	}

	static boolean isModelAnnotationDependency(
			JavaClass source,
			JavaClass target,
			List<String> modelFrameworkPackages) {
		if (!isInAnyPackage(target, modelFrameworkPackages) || !target.isAnnotation()) {
			return false;
		}
		if (source.getAnnotations().stream()
				.anyMatch(annotation -> annotation.getRawType().equals(target))) {
			return true;
		}
		return source.getAllMembers().stream()
				.map(JavaMember::getAnnotations)
				.flatMap(Set::stream)
				.anyMatch(annotation -> annotation.getRawType().equals(target));
	}

	static boolean isCompositionRoot(JavaClass source) {
		return source.isAnnotatedWith(CONFIGURATION);
	}

	static boolean isIgnored(
			JavaClass source,
			JavaClass target,
			List<BaselineLayout.PatternPair> exceptions) {
		return exceptions.stream().anyMatch(exception ->
				classPattern(exception.source()).test(source)
						&& classPattern(exception.target()).test(target));
	}

	static String[] portPackages(HexagonalLayout layout) {
		List<String> ports = new ArrayList<>(layout.inboundPortPackages());
		ports.addAll(layout.outboundPortPackages());
		return ports.toArray(String[]::new);
	}

	static String[] adapterPackages(HexagonalLayout layout) {
		List<String> packages = new ArrayList<>();
		packages.addAll(layout.inboundAdapterPackages());
		packages.addAll(layout.outboundAdapterPackages());
		packages.addAll(layout.mixedAdapterPackages());
		return packages.stream().filter(ArchitectureRuleSupport::present)
				.distinct().toArray(String[]::new);
	}

	static boolean isInAnyPackage(
			JavaClass type, List<String> packagePatterns, String[] additionalPatterns) {
		return packagePatterns.stream().anyMatch(pattern -> matches(type.getPackageName(), pattern))
				|| List.of(additionalPatterns).stream()
				.anyMatch(pattern -> matches(type.getPackageName(), pattern));
	}

	static boolean isInAnyPackage(JavaClass type, List<String> packagePatterns) {
		return packagePatterns.stream()
				.anyMatch(pattern -> matches(type.getPackageName(), pattern)
						|| matches(type.getName(), pattern));
	}

	static boolean matchesAny(String value, List<String> patterns) {
		return patterns.stream().anyMatch(pattern -> matches(value, pattern));
	}

	static boolean matches(String value, String pattern) {
		String regex = pattern
				.replace("..", "\u0000")
				.replace("*", "\u0001")
				.replace(".", "\\.")
				.replace("\u0000", ".*")
				.replace("\u0001", "[^.]*");
		return value.matches(regex);
	}

	static DescribedPredicate<JavaClass> classPattern(String pattern) {
		return new DescribedPredicate<>("classes matching " + pattern) {
			@Override
			public boolean test(JavaClass input) {
				return matches(input.getName(), pattern)
						|| matches(input.getPackageName(), pattern);
			}
		};
	}

	static DescribedPredicate<JavaClass> classPattern(List<String> patterns) {
		return new DescribedPredicate<>("documented class exceptions") {
			@Override
			public boolean test(JavaClass input) {
				return patterns.stream().anyMatch(pattern -> classPattern(pattern).test(input));
			}
		};
	}

	static boolean present(String value) {
		return value != null && !value.isBlank();
	}

	static ArchRule combine(List<ArchRule> rules) {
		if (rules.isEmpty()) {
			return emptyRule();
		}
		CompositeArchRule result = CompositeArchRule.of(rules.get(0));
		for (int index = 1; index < rules.size(); index++) {
			result = result.and(rules.get(index));
		}
		return result;
	}

	static ArchRule emptyRule() {
		return noClasses().should().beAnnotatedWith(NO_OPTIONAL_RULES)
				.as("no optional architecture rules are configured")
				.allowEmptyShould(true);
	}

	private static DescribedPredicate<JavaClass> portPredicate(
			List<String> packages,
			String suffix) {
		return new DescribedPredicate<>("non-sealed interface ending with " + suffix) {
			@Override
			public boolean test(JavaClass item) {
				return isNonSealedInterface(item)
						&& item.getSimpleName().endsWith(suffix)
						&& packages.stream().anyMatch(
						pattern -> matches(item.getPackageName(), pattern));
			}
		};
	}

	static DescribedPredicate<JavaClass> configuredPortPredicate(
			List<String> packages,
			String suffix) {
		return portPredicate(packages, suffix);
	}

	static DescribedPredicate<JavaClass> nonSealedPortInterface() {
		return new DescribedPredicate<>("non-sealed interface") {
			@Override
			public boolean test(JavaClass item) {
				return isNonSealedInterface(item);
			}
		};
	}

	private static boolean isNonSealedInterface(JavaClass item) {
		return item.isInterface() && !item.reflect().isSealed();
	}
}
