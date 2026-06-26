package io.vepo.kafka.tool.inspect;

import java.net.HttpURLConnection;
import java.net.URI;

public final class SchemaRegistryHealthService {

    static String normalizedBase(String schemaRegistryUrl) {
        var trimmed = schemaRegistryUrl.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    public static String statusForUrl(String schemaRegistryUrl) {
        if (schemaRegistryUrl == null || schemaRegistryUrl.isBlank()) {
            return "Not configured";
        }
        try {
            var url = normalizedBase(schemaRegistryUrl) + "/subjects";
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

    private SchemaRegistryHealthService() {}

}
