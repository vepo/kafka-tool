package io.vepo.kafka.tool.inspect.bridge;

import java.util.List;

import io.vepo.kafka.tool.inspect.BrokerConfigEntry;
import io.vepo.kafka.tool.inspect.ClusterMonitorSnapshot;
import io.vepo.kafka.tool.inspect.ConsumerGroupMemberInfo;
import io.vepo.kafka.tool.inspect.ConsumerGroupSummary;
import io.vepo.kafka.tool.inspect.PartitionLagRow;
import io.vepo.kafka.tool.inspect.TopicInfo;
import io.vepo.kafka.tool.inspect.TopicPartitionInfo;
import io.vepo.kafka.tool.settings.KafkaBroker;

public interface KafkaAdminBridge extends AutoCloseable {

    @Override
    void close();

    List<PartitionLagRow> computeConsumerGroupLag(String groupId) throws Exception;

    void connect(KafkaBroker broker) throws Exception;

    List<BrokerConfigEntry> describeBrokerConfig(int brokerId) throws Exception;

    List<ConsumerGroupMemberInfo> describeConsumerGroupMembers(String groupId) throws Exception;

    List<TopicPartitionInfo> describeTopicPartitions(String topic) throws Exception;

    void disconnect();

    void emptyTopic(TopicInfo topic) throws Exception;

    boolean isConnected();

    List<ConsumerGroupSummary> listConsumerGroups() throws Exception;

    List<TopicInfo> listTopics() throws Exception;

    ClusterMonitorSnapshot loadClusterMonitor(String schemaRegistryUrl) throws Exception;

    void verifyClusterReachable(KafkaBroker broker) throws Exception;

}
