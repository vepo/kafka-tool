package io.vepo.kafka.tool.controls.helpers;

import java.util.Optional;

public final class DisplayValue {

    public static String of(Optional<?> optional) {
        return optional.map(DisplayValue::ofNullable).orElse("-");
    }

    public static String ofNullable(Object value) {
        if (value instanceof Optional<?> optional) {
            return of(optional);
        }
        if (value == null) {
            return "-";
        }
        var text = value.toString();
        return text.isBlank() ? "-" : text;
    }

    public static String ofString(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private DisplayValue() {}

}
