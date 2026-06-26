package io.vepo.kafka.tool.consumers;

import io.vepo.kafka.tool.settings.KeySerializer;

public final class KeyFormatter {

    public static String format(byte[] key, KeySerializer serializer) {
	if (key == null) {
	    return "null";
	}
	if (serializer == null) {
	    return "No serializer selected";
	}
	return switch (serializer) {
	    case STRING -> new String(key);
	    case INTEGER -> byteArrayToIntegerString(key);
	    default -> "Not implemented: " + serializer;
	};
    }

    private static String byteArrayToIntegerString(byte[] key) {
	if (key.length != 4) {
	    return "Invalid integer";
	}
	int value = 0;
	for (byte b : key) {
	    value <<= 8;
	    value |= b & 255;
	}
	return Integer.toString(value);
    }

    private KeyFormatter() {
    }

}
