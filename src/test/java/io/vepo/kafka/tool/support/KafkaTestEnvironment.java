package io.vepo.kafka.tool.support;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import io.vepo.kafka.tool.settings.KafkaBroker;

public final class KafkaTestEnvironment {

    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:7.7.1");
    private static final DockerImageName SCHEMA_REGISTRY_IMAGE = DockerImageName.parse("confluentinc/cp-schema-registry:7.7.1");

    private static Network network;
    private static KafkaContainer kafka;
    private static GenericContainer<?> schemaRegistry;

    public static String bootstrapServers() {
        ensureStarted();
        return kafka.getBootstrapServers();
    }

    public static KafkaBroker brokerProfile(boolean withSchemaRegistry) {
        ensureStarted();
        var registryUrl = withSchemaRegistry ? schemaRegistryUrl() : "";
        return new KafkaBroker("test", kafka.getBootstrapServers(), registryUrl);
    }

    public static synchronized void ensureStarted() {
        if (kafka != null) {
            return;
        }
        network = Network.newNetwork();
        kafka = new KafkaContainer(KAFKA_IMAGE).withNetwork(network).withNetworkAliases("kafka");
        kafka.start();
        schemaRegistry = new GenericContainer<>(SCHEMA_REGISTRY_IMAGE)
                                                                      .withNetwork(network)
                                                                      .dependsOn(kafka)
                                                                      .withExposedPorts(8081)
                                                                      .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                                                                      .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9092")
                                                                      .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081");
        schemaRegistry.start();
    }

    public static String schemaRegistryUrl() {
        ensureStarted();
        return "http://%s:%d".formatted(schemaRegistry.getHost(), schemaRegistry.getMappedPort(8081));
    }

    private KafkaTestEnvironment() {}

}
