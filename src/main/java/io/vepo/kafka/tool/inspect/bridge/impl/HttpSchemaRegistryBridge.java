package io.vepo.kafka.tool.inspect.bridge.impl;

import java.net.HttpURLConnection;
import java.net.URI;

import io.vepo.kafka.tool.inspect.SchemaRegistryHealthService;
import io.vepo.kafka.tool.inspect.bridge.SchemaRegistryBridge;

public final class HttpSchemaRegistryBridge implements SchemaRegistryBridge {

    public static HttpSchemaRegistryBridge create() {
        return new HttpSchemaRegistryBridge();
    }

    @Override
    public String healthStatus(String schemaRegistryUrl) {
        if (schemaRegistryUrl == null || schemaRegistryUrl.isBlank()) {
            return "Not configured";
        }
        try {
            var url = SchemaRegistryHealthService.normalizedBase(schemaRegistryUrl) + "/subjects";
            var connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setConnectTimeout(3_000);
            connection.setReadTimeout(3_000);
            connection.setRequestMethod("GET");
            var code = connection.getResponseCode();
            if (code >= 200 && code < 300) {
                return "Reachable";
            }
            return "Unreachable (HTTP %d)".formatted(code);
        } catch (Exception e) {
            return "Unreachable (%s)".formatted(e.getMessage());
        }
    }

}
