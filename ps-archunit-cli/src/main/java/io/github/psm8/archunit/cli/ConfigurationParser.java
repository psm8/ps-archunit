package io.github.psm8.archunit.cli;

import io.github.psm8.archunit.BaselineLayout;
import io.github.psm8.archunit.ConfigurationVisibility;
import io.github.psm8.archunit.DomainOrientedLayout;
import io.github.psm8.archunit.HexagonalLayout;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class ConfigurationParser {
	private static final List<String> TOP_LEVEL_KEYS = List.of(
			"schemaVersion", "tier", "basePackage", "baseline",
			"domainOriented", "hexagonal", "replace", "append");
	private static final List<String> BASELINE_KEYS = List.of(
			"outputs", "dependencyBans", "cyclePatterns", "cycleDependencyIgnores",
			"configurationVisibility", "publicConfigurationClasses",
			"publicConfigurationProperties",
			"allowedApplicationInterfaceBeanTypes", "outputSuffix");
	private static final List<String> DOMAIN_KEYS = List.of(
			"domain", "applicationPackages", "apiPackages", "infrastructurePackages",
			"domainModelFrameworkPackages", "frameworkDependencyPackages",
			"dependencyDirectionIgnores", "transactionAnnotation", "transactionPackages");
	private static final List<String> HEXAGONAL_KEYS = List.of(
			"adapterMode",
			"inboundPortPackages", "outboundPortPackages", "inboundAdapterPackages",
			"outboundAdapterPackages", "mixedAdapterPackages", "portSignatureExceptions",
			"nonAdapterClasses", "useCaseSuffix", "portSuffix", "adapterSuffix");

	ParsedConfiguration parse(Path path) {
		try {
			return parse(Files.readString(path), path);
		} catch (IOException exception) {
			throw new ConfigurationException("cannot read configuration: " + path, exception);
		}
	}

	ParsedConfiguration parse(String yaml) {
		return parse(yaml, null);
	}

	private ParsedConfiguration parse(String yaml, Path source) {
		Map<String, Object> root = map(load(yaml), "configuration root");
		ensureKeys(root, TOP_LEVEL_KEYS, "configuration");
		if (!Objects.equals(number(root, "schemaVersion"), 1)) {
			throw new ConfigurationException("schemaVersion must be 1");
		}
		Tier tier = Tier.parse(required(root, "tier"));
		String basePackage = string(root, "basePackage");
		Map<String, Object> tierSettings = section(root, tier.yamlName());
		ensureKeys(tierSettings, nestedKeysFor(tier), tier.yamlName());
		Map<String, Object> replace = mapOrEmpty(root, "replace");
		Map<String, Object> append = mapOrEmpty(root, "append");
		for (Tier sectionTier : Tier.values()) {
			if (sectionTier.ordinal() > tier.ordinal()) {
				continue;
			}
			Map<String, Object> section = section(root, sectionTier.yamlName());
			ensureKeys(section, nestedKeysFor(sectionTier), sectionTier.yamlName());
			replace = mergeSections(
					replace, mapOrEmpty(section, "replace"), "replace");
			append = mergeSections(
					append, mapOrEmpty(section, "append"), "append");
		}
		ensureKeys(replace, keysFor(tier), "replace");
		ensureKeys(append, keysFor(tier), "append");
		validateScalarSections(replace, append);

		return new ParsedConfiguration(
				tier,
				basePackage,
				buildLayout(tier, basePackage, tierSettings, replace, append),
				source);
	}

	private Object load(String yaml) {
		try {
			return new Yaml(new SafeConstructor(new LoaderOptions())).load(yaml);
		} catch (RuntimeException exception) {
			throw new ConfigurationException("invalid YAML configuration", exception);
		}
	}

	private Object buildLayout(
			Tier tier,
			String basePackage,
			Map<String, Object> tierSettings,
			Map<String, Object> replace,
			Map<String, Object> append) {
		BaselineLayout.Builder baseline = BaselineLayout.builder(basePackage);
		applyBaseline(baseline, replace, append);
		if (tier == Tier.BASELINE) {
			return baseline.build();
		}

		DomainOrientedLayout.Builder domain = DomainOrientedLayout.builder(baseline.build());
		applyDomain(domain, replace, append);
		if (tier == Tier.DOMAIN_ORIENTED) {
			return domain.build();
		}

		HexagonalLayout.Builder hexagonal = HexagonalLayout.builder(domain.build());
		String adapterMode = optionalString(tierSettings, "adapterMode");
		if (adapterMode != null) {
			if (!adapterMode.equals("strict") && !adapterMode.equals("lax")) {
				throw new ConfigurationException("hexagonal.adapterMode must be strict or lax");
			}
			if (adapterMode.equals("lax")) {
				hexagonal.inboundAdapterPackages()
						.outboundAdapterPackages()
						.mixedAdapterPackages(
								"{base}.adapter..", "{base}.*.adapter..");
			}
		}
		applyHexagonal(hexagonal, replace, append);
		return hexagonal.build();
	}

	private void applyBaseline(
			BaselineLayout.Builder builder,
			Map<String, Object> replace,
			Map<String, Object> append) {
		applyPatterns(replace, "outputs", builder::outputs);
		applyPatterns(append, "outputs", builder::addOutputs);
		applyDependencyBans(builder, replace, append);
		applyCombinedPatterns(replace, append, "cyclePatterns", builder::cyclePatterns);
		applyPairs(replace, "cycleDependencyIgnores", builder::ignoreCycleDependency);
		applyPairs(append, "cycleDependencyIgnores", builder::ignoreCycleDependency);
		applyScalar(replace, "configurationVisibility",
				value -> builder.configurationVisibility(parseConfigurationVisibility(value)));
		applyCombinedPatterns(replace, append, "publicConfigurationClasses",
				builder::publicConfigurationClasses);
		applyCombinedPatterns(replace, append, "publicConfigurationProperties",
				builder::publicConfigurationProperties);
		applyCombinedPatterns(replace, append, "allowedApplicationInterfaceBeanTypes",
				builder::allowedApplicationInterfaceBeanTypes);
		applyScalar(replace, "outputSuffix", builder::outputSuffix);
	}

	private void applyDomain(
			DomainOrientedLayout.Builder builder,
			Map<String, Object> replace,
			Map<String, Object> append) {
		applyPatterns(replace, "domain", builder::domain);
		applyPatterns(append, "domain", builder::addDomains);
		applyPatterns(replace, "applicationPackages", builder::applicationPackages);
		applyPatterns(append, "applicationPackages", builder::addApplicationPackages);
		applyPatterns(replace, "apiPackages", builder::apiPackages);
		applyPatterns(append, "apiPackages", builder::addApiPackages);
		applyPatterns(replace, "infrastructurePackages", builder::infrastructurePackages);
		applyPatterns(append, "infrastructurePackages", builder::addInfrastructurePackages);
		applyPatterns(replace, "domainModelFrameworkPackages",
				builder::domainModelFrameworkPackages);
		applyPatterns(append, "domainModelFrameworkPackages",
				builder::addDomainModelFrameworkPackages);
		applyPatterns(replace, "frameworkDependencyPackages",
				builder::frameworkDependencyPackages);
		applyPatterns(append, "frameworkDependencyPackages",
				builder::addFrameworkDependencyPackages);
		applyPairs(replace, "dependencyDirectionIgnores", builder::ignoreDependency);
		applyPairs(append, "dependencyDirectionIgnores", builder::ignoreDependency);
		applyScalar(replace, "transactionAnnotation", builder::transactionAnnotation);
		applyPatterns(replace, "transactionPackages", builder::transactionPackages);
		applyPatterns(append, "transactionPackages", builder::addTransactionPackages);
	}

	private void applyHexagonal(
			HexagonalLayout.Builder builder,
			Map<String, Object> replace,
			Map<String, Object> append) {
		applyPatterns(replace, "inboundPortPackages", builder::inboundPortPackages);
		applyPatterns(append, "inboundPortPackages", builder::addInboundPortPackages);
		applyPatterns(replace, "outboundPortPackages", builder::outboundPortPackages);
		applyPatterns(append, "outboundPortPackages", builder::addOutboundPortPackages);
		applyPatterns(replace, "inboundAdapterPackages", builder::inboundAdapterPackages);
		applyPatterns(append, "inboundAdapterPackages", builder::addInboundAdapterPackages);
		applyPatterns(replace, "outboundAdapterPackages", builder::outboundAdapterPackages);
		applyPatterns(append, "outboundAdapterPackages", builder::addOutboundAdapterPackages);
		applyPatterns(replace, "mixedAdapterPackages", builder::mixedAdapterPackages);
		applyPatterns(append, "mixedAdapterPackages", builder::addMixedAdapterPackages);
		applyCombinedPatterns(replace, append, "portSignatureExceptions",
				builder::portSignatureExceptions);
		applyCombinedPatterns(replace, append, "nonAdapterClasses",
				builder::nonAdapterClasses);
		applyScalar(replace, "useCaseSuffix", builder::useCaseSuffix);
		applyScalar(replace, "portSuffix", builder::portSuffix);
		applyScalar(replace, "adapterSuffix", builder::adapterSuffix);
	}

	private void applyDependencyBans(
			BaselineLayout.Builder builder,
			Map<String, Object> replace,
			Map<String, Object> append) {
		List<BaselineLayout.DependencyBan> bans = new ArrayList<>();
		bans.addAll(dependencyBans(replace.get("dependencyBans")));
		bans.addAll(dependencyBans(append.get("dependencyBans")));
		builder.dependencyBans(bans.toArray(BaselineLayout.DependencyBan[]::new));
	}

	private List<BaselineLayout.DependencyBan> dependencyBans(Object value) {
		if (value == null) {
			return List.of();
		}
		List<BaselineLayout.DependencyBan> result = new ArrayList<>();
		for (Object item : list(value, "dependencyBans")) {
			Map<String, Object> ban = map(item, "dependency ban");
			ensureKeys(ban, List.of("sourcePackages", "bannedPackages", "ignoredDependencies"),
					"dependency ban");
			List<String> sources = strings(ban.get("sourcePackages"), "sourcePackages");
			List<String> targets = strings(ban.get("bannedPackages"), "bannedPackages");
			BaselineLayout.DependencyBan dependencyBan =
					BaselineLayout.DependencyBan.of(sources, targets);
			for (Object pairValue : listOrEmpty(ban.get("ignoredDependencies"), "ignoredDependencies")) {
				Map<String, Object> pair = map(pairValue, "ignored dependency");
				ensureKeys(pair, List.of("source", "target"), "ignored dependency");
				dependencyBan = dependencyBan.ignoring(
						string(pair, "source"), string(pair, "target"));
			}
			result.add(dependencyBan);
		}
		return result;
	}

	private void applyPairs(
			Map<String, Object> section,
			String key,
			PairConsumer consumer) {
		for (Object value : listOrEmpty(section.get(key), key)) {
			Map<String, Object> pair = map(value, key + " pair");
			ensureKeys(pair, List.of("source", "target"), key);
			consumer.accept(string(pair, "source"), string(pair, "target"));
		}
	}

	private void applyPatterns(Map<String, Object> section, String key, PatternConsumer consumer) {
		if (section.containsKey(key)) {
			consumer.accept(strings(section.get(key), key).toArray(String[]::new));
		}
	}

	private void applyCombinedPatterns(
			Map<String, Object> replace,
			Map<String, Object> append,
			String key,
			PatternConsumer consumer) {
		if (!replace.containsKey(key) && !append.containsKey(key)) {
			return;
		}
		List<String> values = new ArrayList<>();
		if (replace.containsKey(key)) {
			values.addAll(strings(replace.get(key), key));
		}
		if (append.containsKey(key)) {
			values.addAll(strings(append.get(key), key));
		}
		consumer.accept(values.toArray(String[]::new));
	}

	private void applyScalar(
			Map<String, Object> section,
			String key,
			ScalarConsumer consumer) {
		if (section.containsKey(key)) {
			consumer.accept(string(section, key));
		}
	}

	private void validateScalarSections(
			Map<String, Object> replace,
			Map<String, Object> append) {
		for (String key : List.of(
				"configurationVisibility", "outputSuffix", "transactionAnnotation", "useCaseSuffix",
				"portSuffix", "adapterSuffix")) {
			if (append.containsKey(key)) {
				throw new ConfigurationException(key + " supports replace only");
			}
		}
	}

	private ConfigurationVisibility parseConfigurationVisibility(String value) {
		return switch (value) {
			case "unrestricted" -> ConfigurationVisibility.UNRESTRICTED;
			case "packagePrivate" -> ConfigurationVisibility.PACKAGE_PRIVATE;
			default -> throw new ConfigurationException(
					"configurationVisibility must be unrestricted or packagePrivate");
		};
	}

	private List<String> keysFor(Tier tier) {
		List<String> keys = new ArrayList<>(BASELINE_KEYS);
		if (tier != Tier.BASELINE) {
			keys.addAll(DOMAIN_KEYS);
		}
		if (tier == Tier.HEXAGONAL) {
			keys.addAll(HEXAGONAL_KEYS);
		}
		return keys;
	}

	private List<String> nestedKeysFor(Tier tier) {
		List<String> keys = new ArrayList<>(List.of("replace", "append"));
		if (tier == Tier.HEXAGONAL) {
			keys.add("adapterMode");
		}
		return keys;
	}

	private static Map<String, Object> mergeSections(
			Map<String, Object> outer,
			Map<String, Object> nested,
			String section) {
		Map<String, Object> merged = new LinkedHashMap<>(outer);
		for (Map.Entry<String, Object> entry : nested.entrySet()) {
			if (merged.containsKey(entry.getKey())) {
				throw new ConfigurationException(
						section + " defines " + entry.getKey() + " more than once");
			}
			merged.put(entry.getKey(), entry.getValue());
		}
		return merged;
	}

	private static void ensureKeys(Map<String, Object> values, Collection<String> allowed, String label) {
		for (String key : values.keySet()) {
			if (!allowed.contains(key)) {
				throw new ConfigurationException("unknown " + label + " key: " + key);
			}
		}
	}

	private static Map<String, Object> map(Object value, String label) {
		if (!(value instanceof Map<?, ?> raw)) {
			throw new ConfigurationException(label + " must be a mapping");
		}
		Map<String, Object> result = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : raw.entrySet()) {
			if (!(entry.getKey() instanceof String key)) {
				throw new ConfigurationException(label + " keys must be strings");
			}
			result.put(key, entry.getValue());
		}
		return result;
	}

	private static Map<String, Object> mapOrEmpty(Map<String, Object> root, String key) {
		if (!root.containsKey(key) || root.get(key) == null) {
			return Map.of();
		}
		return map(root.get(key), key);
	}

	private static Map<String, Object> section(Map<String, Object> root, String key) {
		return mapOrEmpty(root, key);
	}

	private static Object required(Map<String, Object> values, String key) {
		if (!values.containsKey(key) || values.get(key) == null) {
			throw new ConfigurationException(key + " is required");
		}
		return values.get(key);
	}

	private static String string(Map<String, Object> values, String key) {
		Object value = required(values, key);
		if (!(value instanceof String text) || text.isBlank()) {
			throw new ConfigurationException(key + " must be a non-blank string");
		}
		return text;
	}

	private static String optionalString(Map<String, Object> values, String key) {
		if (!values.containsKey(key) || values.get(key) == null) {
			return null;
		}
		return string(values, key);
	}

	private static Number number(Map<String, Object> values, String key) {
		Object value = required(values, key);
		if (!(value instanceof Number number)) {
			throw new ConfigurationException(key + " must be an integer");
		}
		return number;
	}

	private static List<Object> list(Object value, String key) {
		if (!(value instanceof List<?> values)) {
			throw new ConfigurationException(key + " must be a list");
		}
		return new ArrayList<>(values);
	}

	private static List<Object> listOrEmpty(Object value, String key) {
		return value == null ? List.of() : list(value, key);
	}

	private static List<String> strings(Object value, String key) {
		List<String> result = new ArrayList<>();
		for (Object item : list(value, key)) {
			if (!(item instanceof String text) || text.isBlank()) {
				throw new ConfigurationException(key + " entries must be non-blank strings");
			}
			result.add(text);
		}
		return result;
	}

	@FunctionalInterface
	private interface PatternConsumer {
		void accept(String... values);
	}

	@FunctionalInterface
	private interface PairConsumer {
		void accept(String source, String target);
	}

	@FunctionalInterface
	private interface ScalarConsumer {
		void accept(String value);
	}
}
