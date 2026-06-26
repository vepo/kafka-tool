package io.vepo.kafka.tool.settings;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.net.URI;
import java.util.Collection;
import java.util.regex.Pattern;

public final class KafkaBrokerValidator {
    public record ValidationResult(boolean valid, String message) {
        public static ValidationResult ok() {
            return new ValidationResult(true, "");
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
    }

    private static final Pattern BOOTSTRAP_SERVER = Pattern.compile("^[\\w.-]+:\\d{1,5}$");

    public static ValidationResult validate(KafkaBroker broker, Collection<KafkaBroker> existingBrokers) {
        if (isNull(broker)) {
            return ValidationResult.error("Broker is required.");
        }

        var nameResult = validateName(broker.getName(), broker, existingBrokers);
        if (!nameResult.valid()) {
            return nameResult;
        }

        var bootstrapResult = validateBootstrapServers(broker.getBootStrapServers());
        if (!bootstrapResult.valid()) {
            return bootstrapResult;
        }

        return validateSchemaRegistryUrl(broker.getSchemaRegistryUrl());
    }

    public static ValidationResult validateBootstrapServers(String bootstrapServers) {
        if (isNull(bootstrapServers) || bootstrapServers.isBlank()) {
            return ValidationResult.error("Bootstrap servers are required.");
        }

        var servers = bootstrapServers.split(",", -1);
        if (servers.length == 0) {
            return ValidationResult.error("Bootstrap servers are required.");
        }

        for (var server : servers) {
            var trimmed = server.trim();
            if (trimmed.isEmpty()) {
                return ValidationResult.error("Bootstrap servers cannot contain empty entries.");
            }
            if (!BOOTSTRAP_SERVER.matcher(trimmed).matches()) {
                return ValidationResult.error("Bootstrap servers must use host:port format (e.g. localhost:9092).");
            }
            var port = Integer.parseInt(trimmed.substring(trimmed.lastIndexOf(':') + 1));
            if (port < 1 || port > 65535) {
                return ValidationResult.error("Bootstrap server port must be between 1 and 65535.");
            }
        }

        return ValidationResult.ok();
    }

    public static ValidationResult validateName(String name, KafkaBroker broker,
                                                Collection<KafkaBroker> existingBrokers) {
        if (isNull(name) || name.isBlank()) {
            return ValidationResult.error("Name is required.");
        }

        if (nonNull(existingBrokers)) {
            var normalized = name.trim();
            var duplicate = existingBrokers.stream()
                                           .filter(existing -> existing != broker)
                                           .map(KafkaBroker::getName)
                                           .filter(existingName -> nonNull(existingName) && !existingName.isBlank())
                                           .anyMatch(existingName -> existingName.trim().equalsIgnoreCase(normalized));
            if (duplicate) {
                return ValidationResult.error("A broker with this name already exists.");
            }
        }

        return ValidationResult.ok();
    }

    public static ValidationResult validateSchemaRegistryUrl(String schemaRegistryUrl) {
        if (isNull(schemaRegistryUrl) || schemaRegistryUrl.isBlank()) {
            return ValidationResult.ok();
        }

        try {
            var uri = URI.create(schemaRegistryUrl.trim());
            if (isNull(uri.getScheme()) || (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme()))) {
                return ValidationResult.error("Schema Registry URL must start with http:// or https://.");
            }
            if (isNull(uri.getHost()) || uri.getHost().isBlank()) {
                return ValidationResult.error("Schema Registry URL must include a host.");
            }
        } catch (IllegalArgumentException e) {
            return ValidationResult.error("Schema Registry URL is not valid.");
        }

        return ValidationResult.ok();
    }

    private KafkaBrokerValidator() {}
}
