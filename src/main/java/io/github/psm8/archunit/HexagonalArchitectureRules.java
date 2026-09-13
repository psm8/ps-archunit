package io.github.psm8.archunit;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import javax.lang.model.SourceVersion;

import static com.tngtech.archunit.library.Architectures.onionArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit rules for the documented Java hexagonal package contract.
 *
 * <p>Skill basis:
 * {@code clean-ddd-hexagonal/SKILL.md},
 * {@code clean-ddd-hexagonal/references/HEXAGONAL.md}, and
 * {@code clean-ddd-hexagonal/references/TESTING.md}.</p>
 */
public final class HexagonalArchitectureRules {
    private static final String[] FORBIDDEN_FRAMEWORK_PACKAGES = {
            "org.springframework..",
            "jakarta..",
            "javax.."
    };

    private HexagonalArchitectureRules() {
    }

    /**
     * Returns the low-friction, built-in onion architecture rule.
     *
     * @param basePackage consumer application root, for example {@code com.acme.orders}
     * @return built-in ArchUnit onion rule
     * @throws IllegalArgumentException if {@code basePackage} is not a Java package name
     */
    public static ArchRule minimal(String basePackage) {
        Layout layout = Layout.of(basePackage);

        return onionArchitecture()
                .domainModels(layout.domainModels())
                .domainServices(layout.domainServices())
                .applicationServices(layout.applicationServices(), layout.applicationPorts())
                .adapter("in", layout.inboundAdapters())
                .adapter("out", layout.outboundAdapters())
                .withOptionalLayers(true)
                .as("the hexagonal onion architecture under " + layout.basePackage())
                .because("dependencies point inward from adapters to application to domain");
    }

    /**
     * Returns the standard rule set: {@link #minimal(String)} plus the
     * documented hexagonal conventions.
     *
     * @param basePackage consumer application root, for example {@code com.acme.orders}
     * @return composed ArchUnit rule set
     * @throws IllegalArgumentException if {@code basePackage} is not a Java package name
     */
    public static ArchRule standard(String basePackage) {
        Layout layout = Layout.of(basePackage);

        return CompositeArchRule.of(minimal(layout.basePackage()))
                .and(cycleFreeSlices(layout))
                .and(noFrameworkDependencies(layout))
                .and(portPackagesContainInterfaces(layout))
                .and(inboundPortsUseUseCaseSuffix(layout))
                .and(outboundPortsUsePortSuffix(layout))
                .and(outboundAdaptersImplementPorts(layout));
    }

    private static ArchRule cycleFreeSlices(Layout layout) {
        return slices()
                .matching(layout.slicePattern())
                .should()
                .beFreeOfCycles()
                .as("nested packages under " + layout.basePackage() + " are free of dependency cycles")
                .because("architecture tests should preserve cycle-free package boundaries");
    }

    private static ArchRule noFrameworkDependencies(Layout layout) {
        return noClasses()
                .that()
                .resideInAnyPackage(
                        layout.domainModels(),
                        layout.domainServices(),
                        layout.inboundPorts(),
                        layout.outboundPorts())
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(FORBIDDEN_FRAMEWORK_PACKAGES)
                .as("domain and port classes have no Spring, Jakarta, or Javax dependencies")
                .because("the domain and port contracts remain independent of frameworks")
                .allowEmptyShould(true);
    }

    private static ArchRule portPackagesContainInterfaces(Layout layout) {
        return classes()
                .that()
                .resideInAnyPackage(layout.inboundPorts(), layout.outboundPorts())
                .should()
                .beInterfaces()
                .as("classes in port packages are interfaces")
                .because("ports are contracts owned by the application core")
                .allowEmptyShould(true);
    }

    private static ArchRule inboundPortsUseUseCaseSuffix(Layout layout) {
        return classes()
                .that()
                .resideInAnyPackage(layout.inboundPorts())
                .and()
                .areInterfaces()
                .should()
                .haveSimpleNameEndingWith("UseCase")
                .as("inbound port interfaces end with UseCase")
                .because("driving ports describe capabilities invoked by outside actors")
                .allowEmptyShould(true);
    }

    private static ArchRule outboundPortsUsePortSuffix(Layout layout) {
        return classes()
                .that()
                .resideInAnyPackage(layout.outboundPorts())
                .and()
                .areInterfaces()
                .should()
                .haveSimpleNameEndingWith("Port")
                .as("outbound port interfaces end with Port")
                .because("driven ports describe capabilities required from outside systems")
                .allowEmptyShould(true);
    }

    private static ArchRule outboundAdaptersImplementPorts(Layout layout) {
        return classes()
                .that()
                .resideInAnyPackage(layout.outboundAdapters())
                .and()
                .haveSimpleNameEndingWith("Adapter")
                .should(implementAnOutboundPort(layout.outboundPortPrefix()))
                .as("outbound adapter classes implement an outbound port")
                .because("adapters implement application-owned driven contracts")
                .allowEmptyShould(true);
    }

    private static ArchCondition<JavaClass> implementAnOutboundPort(String outboundPortPrefix) {
        return new ArchCondition<>("implement an interface under " + outboundPortPrefix) {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                boolean implementsOutboundPort = item.getAllRawInterfaces().stream()
                        .anyMatch(interfaceType -> isInPackageTree(interfaceType, outboundPortPrefix));

                if (!implementsOutboundPort) {
                    String message = item.getName()
                            + " does not implement an outbound port under "
                            + outboundPortPrefix;
                    events.add(SimpleConditionEvent.violated(item, message));
                }
            }
        };
    }

    private static boolean isInPackageTree(JavaClass javaClass, String packageName) {
        return javaClass.getPackageName().equals(packageName)
                || javaClass.getPackageName().startsWith(packageName + ".");
    }

    private record Layout(String basePackage) {
        private static Layout of(String rawBasePackage) {
            if (rawBasePackage == null
                    || !rawBasePackage.equals(rawBasePackage.trim())
                    || rawBasePackage.isBlank()
                    || !SourceVersion.isName(rawBasePackage)) {
                throw new IllegalArgumentException(
                        "basePackage must be a concrete Java package name: " + rawBasePackage);
            }
            return new Layout(rawBasePackage);
        }

        private String domainModels() {
            return basePackage + ".domain.model..";
        }

        private String domainServices() {
            return basePackage + ".domain.service..";
        }

        private String applicationServices() {
            return basePackage + ".application.service..";
        }

        private String applicationPorts() {
            return basePackage + ".application.port..";
        }

        private String inboundPorts() {
            return basePackage + ".application.port.in..";
        }

        private String outboundPorts() {
            return basePackage + ".application.port.out..";
        }

        private String inboundAdapters() {
            return basePackage + ".adapter.in..";
        }

        private String outboundAdapters() {
            return basePackage + ".adapter.out..";
        }

        private String outboundPortPrefix() {
            return basePackage + ".application.port.out";
        }

        private String slicePattern() {
            return basePackage + ".(**)";
        }
    }
}
