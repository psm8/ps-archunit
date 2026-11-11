package io.github.psm8.archunit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaFieldAccess;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Switchable rules whose findings are violations or advisories depending on
 * their {@link RuleMode}.
 */
final class SwitchableRuleSupport {
	private static final Pattern SETTER_NAME = Pattern.compile("set[A-Z].*");
	private static final String COMPONENT = "org.springframework.stereotype.Component";
	private static final String SERVICE = "org.springframework.stereotype.Service";
	private static final String COMPONENT_SCAN =
			"org.springframework.context.annotation.ComponentScan";

	private SwitchableRuleSupport() {
	}

	static ArchRule select(RuleMode configured, RuleMode wanted, Supplier<ArchRule> rule) {
		return configured == wanted ? rule.get() : ArchitectureRuleSupport.emptyRule();
	}

	static ArchRule setterRule(BaselineLayout layout) {
		return classes().that()
				.resideInAnyPackage(layout.basePackage() + "..")
				.and(notExcepted(layout.setterExceptions()))
				.and(DescribedPredicate.not(builderType()))
				.should(haveNoSetters(layout.basePackage()))
				.as("classes under " + layout.basePackage() + " have no setters")
				.because("mutable state should change through behavior, not setters")
				.allowEmptyShould(true);
	}

	static ArchRule staticFieldRule(BaselineLayout layout) {
		return classes().that()
				.resideInAnyPackage(layout.basePackage() + "..")
				.and(notExcepted(layout.staticFieldExceptions()))
				.should(haveNoStaticNonFinalFields())
				.as("classes under " + layout.basePackage() + " have no static non-final fields")
				.because("static non-final fields are shared mutable global state")
				.allowEmptyShould(true);
	}

	static ArchRule beanCreationRule(BaselineLayout layout) {
		return classes().that()
				.resideInAnyPackage(layout.basePackage() + "..")
				.and().areNotAnnotations()
				.and(notExcepted(layout.beanCreationExceptions()))
				.should(beCreatedExplicitly())
				.as("classes under " + layout.basePackage() + " are not found by component scanning")
				.because("beans are created explicitly by @Bean methods or @Import")
				.allowEmptyShould(true);
	}

	static ArchRule builderRule(BaselineLayout layout) {
		return classes().that()
				.resideInAnyPackage(layout.basePackage() + "..")
				.and(notExcepted(layout.builderExceptions()))
				.should(notBeBuilders())
				.as("classes under " + layout.basePackage() + " are not builders")
				.because("a constructor or factory method is usually enough to construct an object")
				.allowEmptyShould(true);
	}

	static ArchRule defaultMethodRule(
			DomainOrientedLayout layout,
			List<String> corePackages) {
		List<String> packages = new ArrayList<>(layout.domain());
		packages.addAll(corePackages);
		if (packages.isEmpty()) {
			return ArchitectureRuleSupport.emptyRule();
		}
		return classes().that()
				.resideInAnyPackage(packages.toArray(String[]::new))
				.and().areInterfaces()
				.and(notExcepted(layout.defaultMethodExceptions()))
				.should(haveNoDefaultMethods())
				.as("domain and application interfaces have no default methods")
				.because("contracts in the core should not carry hidden implementations")
				.allowEmptyShould(true);
	}

	private static DescribedPredicate<JavaClass> notExcepted(List<String> exceptions) {
		return DescribedPredicate.not(ArchitectureRuleSupport.classPattern(exceptions));
	}

	private static ArchCondition<JavaClass> haveNoSetters(String basePackage) {
		return new ArchCondition<>("have no setters") {
			@Override
			public void check(JavaClass item, ConditionEvents events) {
				for (JavaMethod method : item.getMethods()) {
					if (!isSetterCandidate(method)
							|| mayOverride(method, basePackage)) {
						continue;
					}
					if (SETTER_NAME.matcher(method.getName()).matches()) {
						events.add(SimpleConditionEvent.violated(method,
								method.getFullName() + " is a setter"));
					} else if (writesOwnField(method, method.getName())) {
						events.add(SimpleConditionEvent.violated(method,
								method.getFullName() + " is a fluent setter of field "
										+ method.getName()));
					}
				}
			}
		};
	}

	private static boolean isSetterCandidate(JavaMethod method) {
		Set<JavaModifier> modifiers = method.getModifiers();
		return method.getRawParameterTypes().size() == 1
				&& !modifiers.contains(JavaModifier.PRIVATE)
				&& !modifiers.contains(JavaModifier.STATIC)
				&& !modifiers.contains(JavaModifier.SYNTHETIC)
				&& !modifiers.contains(JavaModifier.BRIDGE);
	}

	private static boolean writesOwnField(JavaMethod method, String fieldName) {
		JavaClass owner = method.getOwner();
		for (JavaFieldAccess access : method.getFieldAccesses()) {
			AccessTarget.FieldAccessTarget target = access.getTarget();
			if (access.getAccessType() == JavaFieldAccess.AccessType.SET
					&& target.getOwner().equals(owner)
					&& target.getName().equals(fieldName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A setter is exempt when it overrides a method that is reported elsewhere
	 * or that it does not own: a reported setter of an analyzed supertype in the
	 * base package, any method of a supertype outside the base package, or a
	 * method of a supertype that could not be inspected. Matching uses name and
	 * arity so that overrides of generic methods are recognized as well.
	 */
	private static boolean mayOverride(JavaMethod method, String basePackage) {
		JavaClass owner = method.getOwner();
		List<JavaClass> supertypes = Stream.concat(
						owner.getAllRawSuperclasses().stream(),
						owner.getAllRawInterfaces().stream())
				.toList();
		for (JavaClass supertype : supertypes) {
			if (!supertype.isFullyImported()) {
				if (!isJdkType(supertype) || jdkTypeDeclares(supertype, method)) {
					return true;
				}
				continue;
			}
			boolean external = !ArchitectureRuleSupport.isInBasePackage(
					supertype.getPackageName(), basePackage);
			for (JavaMethod candidate : supertype.getMethods()) {
				if (isOverriddenBy(candidate, method)
						&& (external || isReportedSetter(candidate))) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isOverriddenBy(JavaMethod candidate, JavaMethod method) {
		Set<JavaModifier> modifiers = candidate.getModifiers();
		boolean visible = modifiers.contains(JavaModifier.PUBLIC)
				|| modifiers.contains(JavaModifier.PROTECTED)
				|| candidate.getOwner().getPackageName()
				.equals(method.getOwner().getPackageName());
		return visible
				&& !modifiers.contains(JavaModifier.PRIVATE)
				&& !modifiers.contains(JavaModifier.STATIC)
				&& candidate.getName().equals(method.getName())
				&& candidate.getRawParameterTypes().size()
				== method.getRawParameterTypes().size();
	}

	private static boolean isReportedSetter(JavaMethod method) {
		return SETTER_NAME.matcher(method.getName()).matches()
				|| writesOwnField(method, method.getName());
	}

	// JDK types are not imported when missing dependencies are not resolved, so
	// they are inspected by reflection; a type that cannot be reflected is
	// treated as uninspectable.
	private static boolean isJdkType(JavaClass type) {
		return type.getName().startsWith("java.");
	}

	private static boolean jdkTypeDeclares(JavaClass type, JavaMethod method) {
		try {
			return reflectedHierarchyDeclares(type.reflect(), method);
		} catch (RuntimeException | LinkageError unavailable) {
			return true;
		}
	}

	private static boolean reflectedHierarchyDeclares(Class<?> type, JavaMethod method) {
		Deque<Class<?>> pending = new ArrayDeque<>(List.of(type));
		Set<Class<?>> visited = new HashSet<>();
		while (!pending.isEmpty()) {
			Class<?> current = pending.pop();
			if (!visited.add(current)) {
				continue;
			}
			for (Method candidate : current.getDeclaredMethods()) {
				int modifiers = candidate.getModifiers();
				if ((Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers))
						&& !Modifier.isStatic(modifiers)
						&& candidate.getName().equals(method.getName())
						&& candidate.getParameterCount() == method.getRawParameterTypes().size()) {
					return true;
				}
			}
			if (current.getSuperclass() != null) {
				pending.push(current.getSuperclass());
			}
			pending.addAll(List.of(current.getInterfaces()));
		}
		return false;
	}

	private static ArchCondition<JavaClass> beCreatedExplicitly() {
		return new ArchCondition<>("be created explicitly") {
			@Override
			public void check(JavaClass item, ConditionEvents events) {
				String name = item.getName();
				if (item.isAnnotatedWith(COMPONENT) || item.isAnnotatedWith(SERVICE)) {
					String annotation = item.isAnnotatedWith(SERVICE) ? "@Service" : "@Component";
					events.add(SimpleConditionEvent.violated(item, name + " is annotated with "
							+ annotation + "; register it with a @Bean method in a @Configuration class"));
				}
				if (item.isAnnotatedWith(COMPONENT_SCAN)) {
					events.add(SimpleConditionEvent.violated(item, name
							+ " enables component scanning; register beans with @Bean methods or @Import"));
				}
				if (item.isAnnotatedWith(ArchitectureRuleSupport.SPRING_BOOT_APPLICATION)) {
					events.add(SimpleConditionEvent.violated(item, name
							+ " enables component scanning through @SpringBootApplication;"
							+ " use @SpringBootConfiguration with @EnableAutoConfiguration"));
				}
			}
		};
	}

	private static ArchCondition<JavaClass> notBeBuilders() {
		return new ArchCondition<>("not be builders") {
			@Override
			public void check(JavaClass item, ConditionEvents events) {
				if (item.getSimpleName().endsWith("Builder") || isBuilderType(item)) {
					events.add(SimpleConditionEvent.violated(item, item.getName()
							+ " is a builder; prefer a constructor or factory method"));
				}
			}
		};
	}

	private static DescribedPredicate<JavaClass> builderType() {
		return DescribedPredicate.describe("builder types", SwitchableRuleSupport::isBuilderType);
	}

	/**
	 * A builder type has a {@code build()} method that returns another type and
	 * at least one fluent method that returns the type itself.
	 */
	private static boolean isBuilderType(JavaClass type) {
		boolean builds = false;
		boolean fluent = false;
		for (JavaMethod method : type.getMethods()) {
			Set<JavaModifier> modifiers = method.getModifiers();
			if (modifiers.contains(JavaModifier.STATIC)
					|| modifiers.contains(JavaModifier.SYNTHETIC)
					|| modifiers.contains(JavaModifier.BRIDGE)) {
				continue;
			}
			boolean returnsItself = method.getRawReturnType().equals(type);
			if (method.getName().equals("build")
					&& method.getRawParameterTypes().isEmpty()
					&& !returnsItself) {
				builds = true;
			} else if (returnsItself) {
				fluent = true;
			}
		}
		return builds && fluent;
	}

	private static ArchCondition<JavaClass> haveNoStaticNonFinalFields() {
		return new ArchCondition<>("have no static non-final fields") {
			@Override
			public void check(JavaClass item, ConditionEvents events) {
				for (JavaField field : item.getFields()) {
					Set<JavaModifier> modifiers = field.getModifiers();
					if (modifiers.contains(JavaModifier.STATIC)
							&& !modifiers.contains(JavaModifier.FINAL)
							&& !modifiers.contains(JavaModifier.SYNTHETIC)) {
						events.add(SimpleConditionEvent.violated(field,
								field.getFullName() + " is a static non-final field"));
					}
				}
			}
		};
	}

	private static boolean isDefaultMethod(JavaMethod method) {
		Set<JavaModifier> modifiers = method.getModifiers();
		return !modifiers.contains(JavaModifier.ABSTRACT)
				&& !modifiers.contains(JavaModifier.STATIC)
				&& !modifiers.contains(JavaModifier.PRIVATE)
				&& !modifiers.contains(JavaModifier.SYNTHETIC);
	}

	private static ArchCondition<JavaClass> haveNoDefaultMethods() {
		return new ArchCondition<>("have no default methods") {
			@Override
			public void check(JavaClass item, ConditionEvents events) {
				List<JavaMethod> defaultMethods = item.getMethods().stream()
						.filter(SwitchableRuleSupport::isDefaultMethod)
						.toList();
				// Reflection needs resolvable supertypes, so it runs only when needed.
				if (defaultMethods.isEmpty()
						|| item.isAnnotatedWith(FunctionalInterface.class)
						|| item.reflect().isSealed()) {
					return;
				}
				for (JavaMethod method : defaultMethods) {
					events.add(SimpleConditionEvent.violated(method,
							method.getFullName() + " is a default interface method"));
				}
			}
		};
	}
}
