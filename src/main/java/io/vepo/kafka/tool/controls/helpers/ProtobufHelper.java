package io.vepo.kafka.tool.controls.helpers;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.google.protobuf.MapEntry;
import com.google.protobuf.Message;

public class ProtobufHelper {
    private static class FieldValueDescriptor implements ValueDescriptor {

	private FieldDescriptor field;

	private FieldValueDescriptor(FieldDescriptor field) {
	    this.field = field;
	}

	public String getName() {
	    return field.getName();
	}

	@Override
	public Type getType() {
	    return field.getType();
	}

	@Override
	public boolean isMapField() {
	    return field.isMapField();
	}

	@Override
	public ValueDescriptor getItemDescriptor() {
	    return new ItemValueDescriptor(field.getMessageType());
	}
    }

    private static class MapValueDescriptor implements ValueDescriptor {

	private String key;

	private MapValueDescriptor(String key) {
	    this.key = key;
	}

	public String getName() {
	    return key;
	}

	@Override
	public Type getType() {
	    throw new IllegalStateException("Map cannot be repeated!");
	}

	@Override
	public boolean isMapField() {
	    return true;
	}

	@Override
	public ValueDescriptor getItemDescriptor() {
	    throw new IllegalStateException("Map cannot be repeated!");
	}
    }

    private static class ItemValueDescriptor implements ValueDescriptor {

	private Descriptor descriptor;

	private ItemValueDescriptor(Descriptor descriptor) {
	    this.descriptor = descriptor;
	}

	@Override
	public String getName() {
	    return descriptor.getName();
	}

	@Override
	public Type getType() {
	    return Type.MESSAGE;
	}

	@Override
	public boolean isMapField() {
	    return false;
	}

	@Override
	public ValueDescriptor getItemDescriptor() {
	    throw new IllegalStateException("Map cannot be repeated!");
	}

    }

    private interface ValueDescriptor {

	String getName();

	Type getType();

	boolean isMapField();

	ValueDescriptor getItemDescriptor();
    }

    private static final Logger logger = LoggerFactory.getLogger(ProtobufHelper.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void fillJsonNode(ObjectNode jsonValue, Object object, ValueDescriptor field) {
//        System.out.println("Field: " + field + " value: " + object);
	if (object instanceof Integer) {
	    jsonValue.put(field.getName(), ((Number) object).intValue());
	} else if (object instanceof Double) {
	    jsonValue.put(field.getName(), ((Number) object).doubleValue());
	} else if (object instanceof Float) {
	    jsonValue.put(field.getName(), ((Number) object).floatValue());
	} else if (object instanceof Long) {
	    jsonValue.put(field.getName(), ((Number) object).longValue());
	} else if (object instanceof Integer) {
	    jsonValue.put(field.getName(), ((Number) object).longValue());
	} else if (object instanceof String) {
	    jsonValue.put(field.getName(), (String) object);
	} else if (object instanceof Message) {
	    jsonValue.set(field.getName(), toJsonNode((Message) object));
	} else if (object instanceof EnumValueDescriptor) {
	    jsonValue.put(field.getName(), object.toString());
	} else if (object instanceof Boolean) {
	    jsonValue.put(field.getName(), (Boolean) object);
	} else if (object instanceof ByteString) {
	    jsonValue.put(field.getName(), ((ByteString) object).toByteArray());
	} else if (object instanceof List && field.isMapField()) {
	    var map = mapper.createObjectNode();
	    ((List<MapEntry<?, ?>>) object).stream().forEachOrdered(item -> {
		fillJsonNode(map, item.getValue(), new MapValueDescriptor(item.getKey().toString()));
	    });
	    jsonValue.set(field.getName(), map);
	} else if (object instanceof List) {
	    var list = mapper.createArrayNode();
	    switch (field.getType()) {
	    case INT32:
	    case FIXED32:
	    case SFIXED32:
	    case SINT32:
	    case UINT32:
		((List) object).stream().forEachOrdered(item -> list.add(((Number) item).intValue()));
		break;
	    case INT64:
	    case FIXED64:
	    case SFIXED64:
	    case SINT64:
	    case UINT64:
		((List) object).stream().forEachOrdered(item -> list.add(((Number) item).longValue()));
		break;
	    case FLOAT:
		((List) object).stream().forEachOrdered(item -> list.add(((Number) item).floatValue()));
		break;
	    case MESSAGE:
		((List) object).stream().forEachOrdered(item -> list.add(toJsonNode((Message) item)));
		break;
	    case STRING:
		((List) object).stream().forEachOrdered(item -> list.add((String) item));
		break;
	    case BOOL:
		((List) object).stream().forEachOrdered(item -> list.add((Boolean) item));
		break;
	    case BYTES:
		((List) object).stream().forEachOrdered(item -> list.add(((ByteString) item).toByteArray()));
		break;
	    case DOUBLE:
		((List) object).stream().forEachOrdered(item -> list.add(((Number) item).doubleValue()));
		break;
	    case ENUM:
		((List) object).stream().forEachOrdered(item -> list.add(((EnumValueDescriptor) item).toString()));
		break;
	    default:
		System.out.println("Field type=" + field.getType());
	    }
	    jsonValue.set(field.getName(), list);
	} else {
	    System.out.println(field.getName() + "->" + object + " Type: " + object.getClass());
	}

    }

    public static String toJson(Message value) {
	System.out.println("Source: " + value);
	try {
	    return mapper.writeValueAsString(toJsonNode(value));
	} catch (JsonProcessingException e) {
	    logger.error(String.format("Could not serialize: %s", value), e);
	    return "";
	}
    }

    public static ObjectNode toJsonNode(Message value) {
	var jsonValue = mapper.createObjectNode();
	value.getAllFields().keySet().stream().sorted(Comparator.comparingInt(FieldDescriptor::getIndex))
		.forEachOrdered(
			field -> fillJsonNode(jsonValue, value.getField(field), new FieldValueDescriptor(field)));
	return jsonValue;
    }

    private ProtobufHelper() {
    }

}
