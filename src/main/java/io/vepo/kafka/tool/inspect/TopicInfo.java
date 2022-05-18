package io.vepo.kafka.tool.inspect;

import java.util.Objects;
import java.util.StringJoiner;

public record TopicInfo(String name, boolean internal) {
}
