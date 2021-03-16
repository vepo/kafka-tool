package io.vepo.kt;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.kafka.clients.admin.RecordsToDelete.beforeOffset;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaAdminService implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(KafkaAdminService.class);

    public enum BrokerStatus {
        IDLE, CONNECTED
    }

    public static class TopicInfo {
        private final String name;
        private final boolean internal;

        private TopicInfo(String name, boolean internal) {
            this.name = name;
            this.internal = internal;
        }

        public String getName() {
            return name;
        }

        public boolean isInternal() {
            return internal;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, internal);
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
            TopicInfo other = (TopicInfo) obj;
            return Objects.equals(name, other.name) && internal == other.internal;
        }

        @Override
        public String toString() {
            return String.format("TopicInfo [name=%s, internal=%s]", name, internal);
        }

    }

    public interface KafkaConnectionWatcher {

        public void statusChanged(BrokerStatus status);
    }

    private BrokerStatus status;
    private AdminClient adminClient = null;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<KafkaConnectionWatcher> watchers;

    public KafkaAdminService() {
        this.watchers = new ArrayList<>();
        this.status = BrokerStatus.IDLE;
    }

    public BrokerStatus getStatus() {
        return status;
    }

    public void connect(String boostraServer, Consumer<BrokerStatus> callback) {
        executor.submit(() -> {
            var properties = new Properties();
            properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, boostraServer);
            adminClient = AdminClient.create(properties);
            status = BrokerStatus.CONNECTED;
            callback.accept(status);
            watchers.forEach(consumer -> consumer.statusChanged(status));
        });
    }

    public void watch(KafkaConnectionWatcher watcher) {
        this.watchers.add(watcher);
    }

    public void emptyTopic(TopicInfo topic) {
        executor.submit(() -> {
            logger.info("Cleaning topic... topic={}", topic);
            if (nonNull(adminClient)) {
                logger.info("Describing topic... topic={}", topic);

                adminClient.describeTopics(asList(topic.getName()))
                           .all()
                           .whenComplete((descs, describeTopicsError) -> {
                               if (isNull(describeTopicsError)) {
                                   adminClient.listOffsets(descs.values()
                                                                .stream()
                                                                .flatMap(desc -> desc.partitions()
                                                                                     .stream()
                                                                                     .map(partition -> new TopicPartition(desc.name(),
                                                                                                                          partition.partition())))
                                                                .collect(Collectors.toMap((TopicPartition t) -> t,
                                                                                          t -> OffsetSpec.latest())))
                                              .all()
                                              .whenComplete((listOffsetResults, listOffsetErros) -> {
                                                  if (isNull(listOffsetErros)) {
                                                      adminClient.deleteRecords(listOffsetResults.entrySet()
                                                                                                 .stream()
                                                                                                 .collect(toMap(entry -> entry.getKey(),
                                                                                                                entry -> beforeOffset(entry.getValue()
                                                                                                                                           .offset()))))
                                                                 .all()
                                                                 .whenComplete((__, deleteRecordsError) -> {
                                                                     if (nonNull(deleteRecordsError)) {
                                                                         logger.error("Error deleting records!",
                                                                                      describeTopicsError);
                                                                     }
                                                                 });
                                                  } else {
                                                      logger.error("Could not list offset!", listOffsetErros);
                                                  }
                                              });
                               } else {
                                   logger.error("Error describing topic!", describeTopicsError);
                               }
                           });
            }
        });
    }

    public void listTopics(Consumer<List<TopicInfo>> callback) {
        executor.submit(() -> {
            if (nonNull(adminClient)) {
                adminClient.listTopics()
                           .listings()
                           .whenComplete((topics, error) -> {
                               if (isNull(error)) {
                                   callback.accept(topics.stream()
                                                         .map(topic -> new TopicInfo(topic.name(), topic.isInternal()))
                                                         .collect(toList()));
                               } else {
                                   callback.accept(emptyList());
                               }
                           });
            } else {
                callback.accept(emptyList());
            }
        });
    }

    @Override
    public void close() {
        if (nonNull(adminClient)) {
            adminClient.close();
        }
    }

}
