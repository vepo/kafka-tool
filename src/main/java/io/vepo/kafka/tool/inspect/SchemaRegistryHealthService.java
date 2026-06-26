package io.vepo.kafka.tool.inspect;

import io.vepo.kafka.tool.inspect.bridge.impl.HttpSchemaRegistryBridge;

public final class SchemaRegistryHealthService {

    private static final HttpSchemaRegistryBridge BRIDGE = HttpSchemaRegistryBridge.create();

    public static String normalizedBase(String schemaRegistryUrl) {
        var trimmed = schemaRegistryUrl.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    public static String statusForUrl(String schemaRegistryUrl) {
        return BRIDGE.healthStatus(schemaRegistryUrl);
    }

    private SchemaRegistryHealthService() {}

}
