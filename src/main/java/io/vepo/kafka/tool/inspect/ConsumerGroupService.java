package io.vepo.kafka.tool.inspect;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vepo.kafka.tool.controls.helpers.DisplayValue;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.common.TopicPartition;

public final class ConsumerGroupService {

    public static ConsumerGroupService create() {
        return new ConsumerGroupService();
    }

    private static String formatAssignment(
                                           org.apache.kafka.clients.admin.MemberAssignment assignment) {
        if (assignment == null || assignment.topicPartitions().isEmpty()) {
            return "-";
        }
        return assignment.topicPartitions().stream()
                         .map(tp -> tp.topic() + "-" + tp.partition())
                         .collect(Collectors.joining(", "));
    }

    private static String formatGroupState(ConsumerGroupListing listing) {
        var groupState = listing.groupState().map(Object::toString);
        var consumerState = listing.state().map(Object::toString);
        return groupState.or(() -> consumerState).orElse("-");
    }

    private ConsumerGroupService() {}

    public List<PartitionLagRow> computeLag(AdminClient client, String groupId) throws Exception {
        Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> offsets = client
                                                                                                 .listConsumerGroupOffsets(groupId)
                                                                                                 .partitionsToOffsetAndMetadata()
                                                                                                 .get();
        if (offsets.isEmpty()) {
            return List.of();
        }
        Map<TopicPartition, OffsetSpec> latestSpecs = offsets.keySet().stream()
                                                             .collect(toMap(tp -> tp, tp -> OffsetSpec.latest()));
        Map<TopicPartition, ListOffsetsResultInfo> endOffsets = client.listOffsets(latestSpecs).all().get();

        List<PartitionLagRow> rows = new ArrayList<>();
        for (var entry : offsets.entrySet()) {
            TopicPartition tp = entry.getKey();
            long committed = entry.getValue().offset();
            ListOffsetsResultInfo endInfo = endOffsets.get(tp);
            long end = endInfo != null ? endInfo.offset() : committed;
            long lag = Math.max(0, end - committed);
            rows.add(new PartitionLagRow(groupId, tp.topic(), tp.partition(), committed, end, lag));
        }
        return rows;
    }

    public List<ConsumerGroupMemberInfo> describeMembers(AdminClient client, String groupId) throws Exception {
        ConsumerGroupDescription description = client.describeConsumerGroups(List.of(groupId)).all().get()
                                                     .get(groupId);
        return description.members().stream()
                          .map(member -> new ConsumerGroupMemberInfo(member.consumerId(), member.clientId(),
                                                                     DisplayValue.ofString(member.host()),
                                                                     formatAssignment(member.assignment())))
                          .collect(Collectors.toList());
    }

    public List<ConsumerGroupSummary> listGroups(AdminClient client) throws Exception {
        return client.listConsumerGroups().all().get().stream()
                     .map(listing -> new ConsumerGroupSummary(listing.groupId(), formatGroupState(listing)))
                     .collect(Collectors.toList());
    }

}
