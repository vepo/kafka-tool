package io.vepo.kt;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;

public class KafkaAdminService implements Closeable {
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

    public void listTopics(Consumer<List<TopicInfo>> callback) {
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
    }

    @Override
    public void close() {
        if (nonNull(adminClient)) {
            adminClient.close();
        }
    }

}
