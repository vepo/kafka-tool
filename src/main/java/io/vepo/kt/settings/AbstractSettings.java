package io.vepo.kt.settings;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vepo.kt.KafkaTool;

public abstract class AbstractSettings implements Cloneable {
    private static final Logger logger = LoggerFactory.getLogger(KafkaTool.class);
    private static final String KAFKA_TOOL_FOLDER = ".kafka-tool";
    private static final ObjectMapper mapper = new ObjectMapper().enable(INDENT_OUTPUT);

    private transient String filename;

    public AbstractSettings(String filename) {
        this.filename = filename;
    }

    void saveProperties() {

        var propertiesFolder = Paths.get(KAFKA_TOOL_FOLDER);
        if (!propertiesFolder.toFile().exists()) {
            propertiesFolder.toFile().mkdir();
        }

        Path propertiesPath = propertiesFolder.resolve(filename);
        try (var writter = Files.newBufferedWriter(propertiesPath, CREATE, TRUNCATE_EXISTING)) {
            writter.write(mapper.writeValueAsString(this));
        } catch (IOException e) {
            logger.error("Error saving file!", e);
        }
    }

    static <T extends AbstractSettings> Optional<T> loadProperties(Class<T> clz, String filename) {
        var propertiesFolder = Paths.get(KAFKA_TOOL_FOLDER);
        if (!propertiesFolder.toFile().exists()) {
            propertiesFolder.toFile().mkdir();
        }

        Path propertiesPath = propertiesFolder.resolve(filename);
        if (propertiesPath.toFile().exists()) {
            try (var reader = Files.newBufferedReader(propertiesPath)) {
                return Optional.of(mapper.readValue(reader.lines().collect(joining()), clz));
            } catch (IOException e) {
                logger.error("Error reading file!", e);
            }
        }
        return Optional.empty();
    }
    
    @Override
    public AbstractSettings clone() {
        try {
            return (AbstractSettings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Cannot clone object");
        }
    }

}
