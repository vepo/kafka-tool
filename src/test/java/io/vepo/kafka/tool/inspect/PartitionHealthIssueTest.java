package io.vepo.kafka.tool.inspect;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PartitionHealthIssueTest {

    @Test
    void issueLabelMatchesType() throws Throwable {
        try (var env = feature("Partition health issue").scenario("Issue label matches type").start()) {
            var issue = new PartitionHealthIssue("t", 0, "1", "1", "1", PartitionHealthIssue.IssueType.OFFLINE);
            env.then("label is offline", () -> assertEquals("Offline", issue.issueLabel()));
        }
    }

    @Test
    void lastOffsetDisplayShowsDashWhenMissing() throws Throwable {
        try (var env = feature("Partition health issue").scenario("Missing last offset display").start()) {
            var issue = new PartitionHealthIssue("t", 0, "1", "1", "1", PartitionHealthIssue.IssueType.OFFLINE);
            env.then("display is dash", () -> assertEquals("-", issue.lastOffsetDisplay()));
        }
    }

}
