package dev.vepo.kafka.tool.web;

import dev.vepo.kafka.tool.core.model.KafkaBroker;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Entity;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Entity
public class KafkaBrokerEntity extends PanacheEntity {
    public String name;
    public String bootStrapServers;
    public String schemaRegistryUrl;

    public static Optional<KafkaBroker> findByName(String name) {
        return KafkaBrokerEntity.<KafkaBrokerEntity>find("name", name)
                .firstResultOptional()
                .map(KafkaBrokerEntity::toDto);
    }

    public static List<KafkaBroker> findAllBrokers() {
        return KafkaBrokerEntity.<KafkaBrokerEntity>findAll(Sort.by("name"))
                .stream()
                .map(KafkaBrokerEntity::toDto)
                .toList();
    }

    private static KafkaBroker toDto(KafkaBrokerEntity broker) {
        return new KafkaBroker(broker.name, broker.bootStrapServers, broker.schemaRegistryUrl);
    }

    @Transactional
    public static void create(KafkaBroker kafkaBroker) {
        persist(toEntity(kafkaBroker));
    }

    private static KafkaBrokerEntity toEntity(KafkaBroker kafkaBroker) {
        var entity = new KafkaBrokerEntity();
        entity.name = kafkaBroker.getName();
        entity.bootStrapServers = kafkaBroker.getBootStrapServers();
        entity.schemaRegistryUrl = kafkaBroker.getSchemaRegistryUrl();
        return entity;
    }
}
