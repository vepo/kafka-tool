package io.vepo.kafka.tool.support;

import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import io.vepo.kafka.tool.settings.KafkaBroker;

public final class KafkaTestFixtures {

    public static void createTopic(KafkaBroker broker, String topic, int partitions) throws Exception {
        var properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, broker.getBootStrapServers());
        try (var admin = AdminClient.create(properties)) {
            admin.createTopics(List.of(new NewTopic(topic, partitions, (short) 1))).all().get(30, TimeUnit.SECONDS);
        }
    }

    public static void produceStrings(KafkaBroker broker, String topic, List<String> values) {
        var properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, broker.getBootStrapServers());
        properties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ACKS_CONFIG, "all");
        try (var producer = new KafkaProducer<String, String>(properties)) {
            for (var value : values) {
                producer.send(new ProducerRecord<>(topic, value)).get(30, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not produce test records", e);
        }
    }

    private KafkaTestFixtures() {}

}
