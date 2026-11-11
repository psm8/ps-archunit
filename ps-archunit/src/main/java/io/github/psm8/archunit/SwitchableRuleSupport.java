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
							|| overridesExternalMethod(method, basePackage)) {
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
	 * Matches by name and arity so that overrides of generic methods, whose
	 * erased parameter types differ, are recognized as well.
	 */
	private static boolean overridesExternalMethod(JavaMethod method, String basePackage) {
		int arity = method.getRawParameterTypes().size();
		JavaClass owner = method.getOwner();
		return Stream.concat(
						owner.getAllRawSuperclasses().stream(),
						owner.getAllRawInterfaces().stream())
				.filter(type -> !ArchitectureRuleSupport.isInBasePackage(
						type.getPackageName(), basePackage))
				.flatMap(type -> type.getMethods().stream())
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

	private static ArchCondition<JavaClass> haveNoDefaultMethods() {
		return new ArchCondition<>("have no default methods") {
			@Override
			public void check(JavaClass item, ConditionEvents events) {
				if (item.isAnnotatedWith(FunctionalInterface.class)
						|| item.reflect().isSealed()) {
					return;
				}
				for (JavaMethod method : item.getMethods()) {
					Set<JavaModifier> modifiers = method.getModifiers();
					if (!modifiers.contains(JavaModifier.ABSTRACT)
							&& !modifiers.contains(JavaModifier.STATIC)
							&& !modifiers.contains(JavaModifier.PRIVATE)
							&& !modifiers.contains(JavaModifier.SYNTHETIC)) {
						events.add(SimpleConditionEvent.violated(method,
								method.getFullName() + " is a default interface method"));
					}
				}
			}
		};
	}
}
