package io.vepo.kafka.tool.inspect;

import static java.util.stream.Collectors.joining;

import java.util.List;

public record ClusterPartitionSummary(String topic, int partition, Long lastOffset, String leader, String replicas,
                                      String isr, List<PartitionHealthIssue.IssueType> issues) {

    public String lastOffsetDisplay() {
        return lastOffset == null ? "-" : String.valueOf(lastOffset);
    }

    public String statusDisplay() {
        if (issues.isEmpty()) {
            return "Healthy";
        }
        return issues.stream().map(PartitionHealthIssue.IssueType::label).collect(joining(", "));
    }

}
