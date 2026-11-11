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

import java.util.ArrayList;
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

	private SwitchableRuleSupport() {
	}

	static ArchRule select(RuleMode configured, RuleMode wanted, Supplier<ArchRule> rule) {
		return configured == wanted ? rule.get() : ArchitectureRuleSupport.emptyRule();
	}

	static ArchRule setterRule(BaselineLayout layout) {
		return classes().that()
				.resideInAnyPackage(layout.basePackage() + "..")
				.and(notExcepted(layout.setterExceptions()))
				.should(haveNoSetters())
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

	private static ArchCondition<JavaClass> haveNoSetters() {
		return new ArchCondition<>("have no setters") {
			@Override
			public void check(JavaClass item, ConditionEvents events) {
				for (JavaMethod method : item.getMethods()) {
					if (!isSetterCandidate(method)
							|| mayOverride(method)) {
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
	 * Overrides are exempt wherever the supertype lives: a setter declared by an
	 * analyzed supertype is reported at its declaration. A supertype that could
	 * not be inspected is treated as declaring the method. Matching uses name and
	 * arity so that overrides of generic methods are recognized as well.
	 */
	private static boolean mayOverride(JavaMethod method) {
		int arity = method.getRawParameterTypes().size();
		JavaClass owner = method.getOwner();
		List<JavaClass> supertypes = Stream.concat(
						owner.getAllRawSuperclasses().stream(),
						owner.getAllRawInterfaces().stream())
				.toList();
		if (supertypes.stream().anyMatch(type -> !type.isFullyImported())) {
			return true;
		}
		return supertypes.stream()
				.flatMap(type -> type.getMethods().stream())
				.filter(candidate -> !candidate.getModifiers().contains(JavaModifier.PRIVATE)
						&& !candidate.getModifiers().contains(JavaModifier.STATIC))
				.anyMatch(candidate -> candidate.getName().equals(method.getName())
						&& candidate.getRawParameterTypes().size() == arity);
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
