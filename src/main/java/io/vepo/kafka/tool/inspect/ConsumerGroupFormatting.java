package io.vepo.kafka.tool.inspect;

import static java.util.stream.Collectors.joining;

import org.apache.kafka.clients.admin.MemberAssignment;

public final class ConsumerGroupFormatting {

    public static long computeLag(long committedOffset, long endOffset) {
        return Math.max(0, endOffset - committedOffset);
    }

    public static String formatAssignment(MemberAssignment assignment) {
        if (assignment == null || assignment.topicPartitions().isEmpty()) {
            return "-";
        }
        return assignment.topicPartitions().stream()
                         .map(tp -> tp.topic() + "-" + tp.partition())
                         .collect(joining(", "));
    }

    private ConsumerGroupFormatting() {}

}
