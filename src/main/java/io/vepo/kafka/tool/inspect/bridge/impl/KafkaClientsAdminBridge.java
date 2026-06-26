package io.vepo.kafka.tool.inspect.bridge.impl;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.apache.kafka.clients.admin.RecordsToDelete.beforeOffset;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartition;

import io.vepo.kafka.tool.inspect.BrokerConfigEntry;
import io.vepo.kafka.tool.inspect.ClusterMonitorSnapshot;
import io.vepo.kafka.tool.inspect.ConsumerGroupMemberInfo;
import io.vepo.kafka.tool.inspect.ConsumerGroupSummary;
import io.vepo.kafka.tool.inspect.PartitionLagRow;
import io.vepo.kafka.tool.inspect.TopicInfo;
import io.vepo.kafka.tool.inspect.TopicPartitionInfo;
import io.vepo.kafka.tool.inspect.bridge.KafkaAdminBridge;
import io.vepo.kafka.tool.inspect.bridge.SchemaRegistryBridge;
import io.vepo.kafka.tool.inspect.bridge.internal.ClusterMonitorOperations;
import io.vepo.kafka.tool.inspect.bridge.internal.ConsumerGroupOperations;
import io.vepo.kafka.tool.settings.KafkaBroker;

public final class KafkaClientsAdminBridge implements KafkaAdminBridge {

    private static Properties adminProperties(KafkaBroker kafkaBroker) {
        var properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker.getBootStrapServers());
        properties.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "8000");
        properties.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "8000");
        return properties;
    }

    public static KafkaClientsAdminBridge create(SchemaRegistryBridge schemaRegistryBridge) {
        return new KafkaClientsAdminBridge(schemaRegistryBridge);
    }

    private final SchemaRegistryBridge schemaRegistryBridge;
    private final ClusterMonitorOperations clusterMonitorOperations;
    private final ConsumerGroupOperations consumerGroupOperations = new ConsumerGroupOperations();
    private AdminClient adminClient;

    private KafkaClientsAdminBridge(SchemaRegistryBridge schemaRegistryBridge) {
        this.schemaRegistryBridge = schemaRegistryBridge;
        this.clusterMonitorOperations = new ClusterMonitorOperations(schemaRegistryBridge);
    }

    @Override
    public void close() {
        closeClient();
    }

    private void closeClient() {
        if (adminClient != null) {
            adminClient.close();
            adminClient = null;
        }
    }

    @Override
    public List<PartitionLagRow> computeConsumerGroupLag(String groupId) throws Exception {
        requireClient();
        return consumerGroupOperations.computeLagRows(adminClient, groupId);
    }

    @Override
    public void connect(KafkaBroker broker) throws Exception {
        closeClient();
        adminClient = AdminClient.create(adminProperties(broker));
        adminClient.describeCluster().nodes().get(8, TimeUnit.SECONDS);
    }

    private void deleteRecords(Map<TopicPartition, ListOffsetsResultInfo> listOffsetResults) {
        adminClient.deleteRecords(listOffsetResults.entrySet()
                                                   .stream()
                                                   .collect(toMap(Map.Entry::getKey,
                                                                  entry -> beforeOffset(entry.getValue().offset()))))
                   .all();
    }

    @Override
    public List<BrokerConfigEntry> describeBrokerConfig(int brokerId) throws Exception {
        requireClient();
        return clusterMonitorOperations.describeBrokerConfig(adminClient, brokerId);
    }

    @Override
    public List<ConsumerGroupMemberInfo> describeConsumerGroupMembers(String groupId) throws Exception {
        requireClient();
        return consumerGroupOperations.describeMembers(adminClient, groupId);
    }

    @Override
    public List<TopicPartitionInfo> describeTopicPartitions(String topic) throws Exception {
        requireClient();
        var descriptions = adminClient.describeTopics(List.of(topic)).allTopicNames().get();
        var description = descriptions.get(topic);
        if (description == null) {
            return List.of();
        }
        Map<TopicPartition, OffsetSpec> earliest = description.partitions().stream()
                                                              .map(info -> new TopicPartition(topic, info.partition()))
                                                              .collect(toMap(tp -> tp, tp -> OffsetSpec.earliest()));
        Map<TopicPartition, OffsetSpec> latest = description.partitions().stream()
                                                            .map(info -> new TopicPartition(topic, info.partition()))
                                                            .collect(toMap(tp -> tp, tp -> OffsetSpec.latest()));
        var beginning = adminClient.listOffsets(earliest).all().get();
        var end = adminClient.listOffsets(latest).all().get();
        return description.partitions().stream()
                          .map(info -> {
                              var tp = new TopicPartition(topic, info.partition());
                              long beginningOffset = beginning.get(tp).offset();
                              long endOffset = end.get(tp).offset();
                              return new TopicPartitionInfo(info.partition(), beginningOffset, endOffset);
                          })
                          .toList();
    }

    @Override
    public void disconnect() {
        closeClient();
    }

    @Override
    public void emptyTopic(TopicInfo topic) throws Exception {
        requireClient();
        var descriptions = adminClient.describeTopics(asList(topic.getName())).allTopicNames().get();
        deleteRecords(listLatestOffsets(descriptions));
    }

    @Override
    public boolean isConnected() {
        return adminClient != null;
    }

    @Override
    public List<ConsumerGroupSummary> listConsumerGroups() throws Exception {
        requireClient();
        return consumerGroupOperations.listGroups(adminClient);
    }

    private Map<TopicPartition, ListOffsetsResultInfo> listLatestOffsets(Map<String, TopicDescription> descriptions)
            throws Exception {
        var specs = descriptions.values()
                                .stream()
                                .flatMap(desc -> desc.partitions()
                                                     .stream()
                                                     .map(partition -> new TopicPartition(desc.name(), partition.partition())))
                                .collect(toMap(tp -> tp, tp -> OffsetSpec.latest()));
        return adminClient.listOffsets(specs).all().get();
    }

    @Override
    public List<TopicInfo> listTopics() throws Exception {
        requireClient();
        return adminClient.listTopics().listings().get().stream()
                          .map(topic -> new TopicInfo(topic.name(), topic.isInternal()))
                          .toList();
    }

    @Override
    public ClusterMonitorSnapshot loadClusterMonitor(String schemaRegistryUrl) throws Exception {
        requireClient();
        return clusterMonitorOperations.loadSnapshot(adminClient, schemaRegistryUrl);
    }

    private void requireClient() {
        if (adminClient == null) {
            throw new IllegalStateException("Not connected to a broker.");
        }
    }

    @Override
    public void verifyClusterReachable(KafkaBroker broker) throws Exception {
        try (var client = AdminClient.create(adminProperties(broker))) {
            client.describeCluster().nodes().get(8, TimeUnit.SECONDS);
        }
    }

}
