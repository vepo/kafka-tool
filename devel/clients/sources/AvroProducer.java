//usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 19
//REPOS mavencentral,confluent-packages=https://packages.confluent.io/maven/
//DEPS org.slf4j:slf4j-api:2.0.9
//DEPS org.slf4j:slf4j-simple:2.0.9
//DEPS org.apache.kafka:kafka-clients:3.5.1
//DEPS io.confluent:kafka-avro-serializer:7.4.0
//SOURCES /sources

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.reflect.ReflectData;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import model.Temperature;

public class AvroProducer {
    private static final Logger logger = LoggerFactory.getLogger(AvroProducer.class);
    private static final int NUM_SENSORS = 50;

    public static Object fieldValue(Object object, String fieldName) {
        try {
            var field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    public static GenericData.Record mapObjectToRecord(Object object) {
        Objects.requireNonNull(object, "POJO object must not be null");
        final Schema schema = ReflectData.get().getSchema(object.getClass());
        final GenericData.Record record = new GenericData.Record(schema);
        schema.getFields()
              .forEach(r -> record.put(r.name(), fieldValue(object, r.name())));
        return record;
    }

    private static final Random RAND = new SecureRandom();

    private static double nextValue(String id, Double prevValue) {
        if (Objects.isNull(prevValue)) {
            return 10 + RAND.nextInt(10);
        } else {
            return prevValue + (RAND.nextDouble() - 0.5);
        }
    }

    public static void main(String[] args) {
        logger.info("Creating Producer...");
        var running = new AtomicBoolean(true);
        var latch = new CountDownLatch(1);
        Runtime.getRuntime()
               .addShutdownHook(new Thread("shutdown-watch") {
                   @Override
                   public void run() {
                       running.set(false);
                       try {
                           latch.await();
                       } catch (InterruptedException ee) {
                           Thread.currentThread().interrupt();
                       }
                   }
               });

        var listOfSensors = IntStream.range(0, NUM_SENSORS)
                                     .mapToObj(i -> UUID.randomUUID().toString())
                                     .collect(Collectors.toList());

        var tempCache = new HashMap<String, Double>(NUM_SENSORS);
        var producerConfigs = new Properties();
        producerConfigs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-0:9092,kafka-1:9095,kafka-2:9098");
        producerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerConfigs.put("schema.registry.url", "http://schema-registry:8081");
        try (var producer = new KafkaProducer<String, GenericData.Record>(producerConfigs)) {
            while (running.get()) {
                var id = UUID.randomUUID().toString();
                for (var sensor : listOfSensors) {
                    var data = new Temperature(sensor,
                                               tempCache.compute(sensor, AvroProducer::nextValue),
                                               System.currentTimeMillis());
                    logger.info("New sensor data={}", data);
                    var metadata = producer.send(new ProducerRecord<>("temperature", sensor,
                                                                      mapObjectToRecord(data)))
                                           .get();
                    logger.info("Metadata: topic={} partition={} offset={} timestamp={}",
                                metadata.topic(),
                                metadata.partition(),
                                metadata.offset(), metadata.timestamp());
                }
                Thread.sleep(10_000);
            }
            latch.countDown();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ee) {
            ee.printStackTrace();
        }
    }
}
