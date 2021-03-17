package io.vepo.kt.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

public class KafkaSettings implements Settings<KafkaSettings>, Cloneable {
    static final String KAFKA_SETTINGS_FILE = "kafka-properties.json";
    @JsonProperty("bootStrapServers")
    private String bootStrapServers;
    @JsonProperty("schemaRegistryUrl")
    private String schemaRegistryUrl;
    @JsonProperty("topic")
    private String topic;

    public KafkaSettings() {
    }

    public KafkaSettings(String bootStrapServers, String schemaRegistryUrl, String topic) {
        this.bootStrapServers = bootStrapServers;
        this.schemaRegistryUrl = schemaRegistryUrl;
        this.topic = topic;
    }

    public String bootStrapServers() {
        return bootStrapServers;
    }

    public void bootStrapServers(String bootStrapServers) {
        this.bootStrapServers = bootStrapServers;
    }


    public String schemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    public void schemaRegistryUrl(String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    public String topic() {
        return topic;
    }

    public void topic(String topic) {
        this.topic = topic;
    }

    @Override
    public void save() {
        Settings.save(KAFKA_SETTINGS_FILE, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaSettings that = (KafkaSettings) o;
        return Objects.equals(bootStrapServers, that.bootStrapServers) && Objects.equals(schemaRegistryUrl, that.schemaRegistryUrl) && Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bootStrapServers, schemaRegistryUrl, topic);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", KafkaSettings.class.getSimpleName() + "[", "]")
                .add("bootStrapServers='" + bootStrapServers + "'")
                .add("schemaRegistryUrl='" + schemaRegistryUrl + "'")
                .add("topic='" + topic + "'")
                .toString();
    }

    @Override
    public KafkaSettings clone() {
        try {
            return (KafkaSettings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
