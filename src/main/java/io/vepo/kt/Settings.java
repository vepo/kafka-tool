package io.vepo.kt;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Objects.isNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Settings implements Cloneable {
    private static final Logger logger = LoggerFactory.getLogger(KafkaTool.class);
    private static final String KAFKA_TOOL_FOLDER = ".kafka-tool";
    private static final String KAFKA_SETTINGS_FILE = "kafka-properties.json";
    private static final AtomicReference<Settings> INSTANCE = new AtomicReference<Settings>();
    private static final ObjectMapper mapper = new ObjectMapper().enable(INDENT_OUTPUT);

    public static Settings getInstance() {
        return INSTANCE.updateAndGet(settings -> {
            if (isNull(settings)) {
                settings = loadProperties().orElseGet(Settings::new);
            }
            return settings;
        });
    }

    public static void readAndUpdate(Consumer<Settings> consumer) {
        var instance = loadProperties().orElseGet(Settings::new);
        consumer.accept(instance);
        instance.saveProperties();
    }

    private String bootStrapServers;
    private String schemaRegistryUrl;
    private String topic;

    private static Optional<Settings> loadProperties() {
        var propertiesFolder = Paths.get(KAFKA_TOOL_FOLDER);
        if (!propertiesFolder.toFile().exists()) {
            propertiesFolder.toFile().mkdir();
        }

        Path propertiesPath = propertiesFolder.resolve(KAFKA_SETTINGS_FILE);
        if (propertiesPath.toFile().exists()) {
            try (var reader = Files.newBufferedReader(propertiesPath)) {
                return Optional.of(mapper.readValue(reader.lines().collect(Collectors.joining()), Settings.class));
            } catch (IOException e) {
                logger.error("Error reading file!", e);
            }
        }
        return Optional.empty();
    }

    private void saveProperties() {

        var propertiesFolder = Paths.get(KAFKA_TOOL_FOLDER);
        if (!propertiesFolder.toFile().exists()) {
            propertiesFolder.toFile().mkdir();
        }

        Path propertiesPath = propertiesFolder.resolve(KAFKA_SETTINGS_FILE);
        try (var writter = Files.newBufferedWriter(propertiesPath, CREATE, TRUNCATE_EXISTING)) {
            writter.write(mapper.writeValueAsString(this));
        } catch (IOException e) {
            logger.error("Error saving file!", e);
        }
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
        Settings other = (Settings) obj;
        return Objects.equals(bootStrapServers, other.bootStrapServers)
                && Objects.equals(schemaRegistryUrl, other.schemaRegistryUrl) && Objects.equals(topic, other.topic);
    }

    @Override
    public String toString() {
        return String.format("Settings [bootStrapServers=%s, schemaRegistryUrl=%s, topic=%s]", bootStrapServers,
                             schemaRegistryUrl, topic);
    }

    @Override
    public Settings clone() {
        try {
            return (Settings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Cannot clone object");
        }
    }

}
