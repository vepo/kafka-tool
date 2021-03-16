package io.vepo.kt.settings;

import static java.util.Objects.isNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class KafkaSettings extends AbstractSettings {
    private static final String KAFKA_SETTINGS_FILE = "kafka-properties.json";
    private static final AtomicReference<KafkaSettings> INSTANCE = new AtomicReference<KafkaSettings>();

    public static KafkaSettings getInstance() {
        return INSTANCE.updateAndGet(settings -> {
            if (isNull(settings)) {
                settings = loadProperties(KafkaSettings.class, KAFKA_SETTINGS_FILE).orElseGet(KafkaSettings::new);
            }
            return settings;
        });
    }

    public static void readAndUpdate(Consumer<KafkaSettings> consumer) {
        var instance = loadProperties(KafkaSettings.class, KAFKA_SETTINGS_FILE).orElseGet(KafkaSettings::new);
        consumer.accept(instance);
        instance.saveProperties();
    }

    private String bootStrapServers;
    private String schemaRegistryUrl;
    private String topic;

    public KafkaSettings() {
        super(KAFKA_SETTINGS_FILE);
    }

    public String getBootStrapServers() {
        return bootStrapServers;
    }

    public void setBootStrapServers(String bootStrapServers) {
        this.bootStrapServers = bootStrapServers;
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    public void setSchemaRegistryUrl(String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bootStrapServers, schemaRegistryUrl, topic);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        KafkaSettings other = (KafkaSettings) obj;
        return Objects.equals(bootStrapServers, other.bootStrapServers)
                && Objects.equals(schemaRegistryUrl, other.schemaRegistryUrl) && Objects.equals(topic, other.topic);
    }

    @Override
    public String toString() {
        return String.format("Settings [bootStrapServers=%s, schemaRegistryUrl=%s, topic=%s]", bootStrapServers,
                             schemaRegistryUrl, topic);
    }

    @Override
    public KafkaSettings clone() {
        return (KafkaSettings) super.clone();
    }

}
