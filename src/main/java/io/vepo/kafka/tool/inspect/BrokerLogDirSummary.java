package io.vepo.kafka.tool.inspect;

import java.util.Locale;

public record BrokerLogDirSummary(int brokerId, String logDir, long totalBytes, String error) {

    public String formattedSize() {
        return formatBytes(totalBytes);
    }

    public static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "—";
        }
        if (bytes < 1024) {
            return "%d B".formatted(bytes);
        }
        if (bytes < 1024 * 1024) {
            return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        }
        if (bytes < 1024L * 1024 * 1024) {
            return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024));
        }
        return String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

}
