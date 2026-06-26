package io.vepo.kafka.tool.settings;

final class KafkaSettingsCache {

    private static KafkaSettings kafkaSettings;

    static KafkaSettings get() {
        if (kafkaSettings == null) {
            kafkaSettings = Settings.loadProperties(KafkaSettings.class, Settings.KAFKA_SETTINGS_FILE)
                                    .orElseGet(KafkaSettings::new);
        }
        return kafkaSettings;
    }

    static void resetForTesting() {
        kafkaSettings = null;
    }

    private KafkaSettingsCache() {}

}
