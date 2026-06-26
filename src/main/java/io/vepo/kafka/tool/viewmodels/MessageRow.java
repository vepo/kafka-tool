package io.vepo.kafka.tool.viewmodels;

import io.vepo.kafka.tool.consumers.KeyFormatter;
import io.vepo.kafka.tool.inspect.KafkaMessage;
import io.vepo.kafka.tool.inspect.MessageMetadata;
import io.vepo.kafka.tool.settings.KeySerializer;

public final class MessageRow {

    private final String displayKey;
    private final String displayValue;
    private final long offset;
    private final String rawValue;

    public MessageRow(String displayKey, String displayValue, long offset, String rawValue) {
	this.displayKey = displayKey;
	this.displayValue = displayValue;
	this.offset = offset;
	this.rawValue = rawValue;
    }

    public static MessageRow from(KafkaMessage message, MessageMetadata metadata, KeySerializer keySerializer) {
	return new MessageRow(
		KeyFormatter.format(message.getKey(), keySerializer),
		message.getValue(),
		metadata.offset(),
		message.getValue());
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

    public String getRawValue() {
	return rawValue;
    }

}
