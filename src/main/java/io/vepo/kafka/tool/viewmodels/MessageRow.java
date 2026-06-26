package io.vepo.kafka.tool.viewmodels;

import io.vepo.kafka.tool.consumers.KeyFormatter;
import io.vepo.kafka.tool.inspect.KafkaMessage;
import io.vepo.kafka.tool.inspect.MessageMetadata;
import io.vepo.kafka.tool.settings.KeySerializer;

public final class MessageRow {

    public static MessageRow from(KafkaMessage message, MessageMetadata metadata, KeySerializer keySerializer) {
        return new MessageRow(
                              metadata.partition(),
                              metadata.offset(),
                              metadata.timestamp(),
                              KeyFormatter.format(message.getKey(), keySerializer),
                              message.getValue(),
                              message.getValue());
    }

    private final int partition;
    private final long offset;
    private final long timestamp;
    private final String displayKey;
    private final String displayValue;

    private final String rawValue;

    public MessageRow(int partition, long offset, long timestamp, String displayKey, String displayValue,
                      String rawValue) {
        this.partition = partition;
        this.offset = offset;
        this.timestamp = timestamp;
        this.displayKey = displayKey;
        this.displayValue = displayValue;
        this.rawValue = rawValue;
    }

    public String getDisplayKey() {
        return displayKey;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public long getOffset() {
        return offset;
    }

    public int getPartition() {
        return partition;
    }

    public String getRawValue() {
        return rawValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
