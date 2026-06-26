package io.vepo.kafka.tool.inspect;

public record PartitionHealthIssue(String topic, int partition, String leader, String replicas, String isr,
                                   IssueType issueType, Long lastOffset) {

    public PartitionHealthIssue(String topic, int partition, String leader, String replicas, String isr,
                                IssueType issueType) {
        this(topic, partition, leader, replicas, isr, issueType, null);
    }

    public enum IssueType {
        UNDER_REPLICATED("Under-replicated"),
        OFFLINE("Offline"),
        LEADER_NOT_PREFERRED("Leader not preferred");

        private final String label;

        IssueType(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public String issueLabel() {
        return issueType.label();
    }

    public String lastOffsetDisplay() {
        return lastOffset == null ? "-" : String.valueOf(lastOffset);
    }

}
