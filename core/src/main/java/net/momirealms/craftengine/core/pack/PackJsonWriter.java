package net.momirealms.craftengine.core.pack;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import net.momirealms.craftengine.core.util.GsonHelper;
import org.apache.commons.io.output.StringBuilderWriter;

import java.io.IOException;
import java.io.Writer;

final class PackJsonWriter extends JsonWriter {
    private PackJsonWriter(Writer out) {
        super(out);
    }

    static String toJson(JsonElement json) {
        StringBuilderWriter output = new StringBuilderWriter() {
            // Avoid Writer's synchronized char-array fallback for JsonWriter's small writes.
            @Override
            public void write(int value) {
                getBuilder().append((char) value);
            }

            @Override
            public void write(String value, int offset, int length) {
                getBuilder().append(value, offset, offset + length);
            }
        };
        GsonHelper.get().toJson(json, new PackJsonWriter(output));
        return output.toString();
    }

    @Override
    public JsonWriter value(Number value) throws IOException {
        if (value != null) {
            String number = value.toString();
            // Gson validates parsed numbers with a new regex Matcher for every value.
            // Validate the same JSON number grammar without allocating a Matcher.
            if (isJsonNumber(number)) return jsonValue(number);
        }
        return super.value(value);
    }

    private static boolean isJsonNumber(String value) {
        int length = value.length();
        int index = 0;
        if (index < length && value.charAt(index) == '-') index++;
        if (index == length) return false;
        char first = value.charAt(index++);
        if (first >= '1' && first <= '9') {
            while (index < length && isDigit(value.charAt(index))) index++;
        } else if (first != '0') {
            return false;
        }
        if (index < length && value.charAt(index) == '.') {
            int start = ++index;
            while (index < length && isDigit(value.charAt(index))) index++;
            if (index == start) return false;
        }
        if (index < length && (value.charAt(index) == 'e' || value.charAt(index) == 'E')) {
            index++;
            if (index < length && (value.charAt(index) == '+' || value.charAt(index) == '-')) index++;
            int start = index;
            while (index < length && isDigit(value.charAt(index))) index++;
            if (index == start) return false;
        }
        return index == length;
    }

    private static boolean isDigit(char value) {
        return value >= '0' && value <= '9';
    }
}
