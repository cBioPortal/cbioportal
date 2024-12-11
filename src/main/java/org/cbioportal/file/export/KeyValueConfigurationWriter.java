package org.cbioportal.file.export;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.RecordComponent;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A serializer for Java records that serializes them the cBioPortal key-value configuration format.
 * e.g. meta_study.txt
 */
public class KeyValueConfigurationWriter {
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Record.class, new KeyValueGenericRecordSerializer());
        mapper.registerModule(module);
    }

    private final Writer writer;

    /**
     * @param writer - the writer to write the key-value configuration to
     *               e.g. StringWriter, FileWriter
     */
    public KeyValueConfigurationWriter(Writer writer) {
        this.writer = writer;
    }    
    
    /**
     * Write a record to the output writer
     */
    public void writeRecord(Record record) {
        try {
            mapper.writeValue(writer, record);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

/**
 * A custom Jackson serializer for Java records that serializes them the cBioPortal key-value configuration format.
 * e.g. meta_study.txt
 */
class KeyValueGenericRecordSerializer extends JsonSerializer<Record> {

    public static final Pattern NEW_WORD_BOUNDARY = Pattern.compile("([a-z])([A-Z])");

    @Override
    public void serialize(Record record, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        for (RecordComponent component : record.getClass().getRecordComponents()) {
            // Check for @JsonProperty annotation
            JsonProperty jsonProperty = component.getAnnotation(JsonProperty.class);
            String key = (jsonProperty != null) ? jsonProperty.value() : toSnakeCase(component.getName());

            // Get the field value using its accessor
            Object value;
            try {
                value = component.getAccessor().invoke(record);
            } catch (Exception e) {
                throw new IOException("Error accessing record component value for " + component.getName(), e);
            }

            if (value instanceof Optional<?> optional) {
                // Unwrap Optional
                if (optional.isPresent()) {
                    // Write key-value pair to the output
                    gen.writeRaw(key + ": " + optional.get() + "\n");
                }
            } else {
                // Write key-value pair to the output
                gen.writeRaw(key + ": " + value + "\n");
            }
        }
    }

    /**
     * Utility method to convert camelCase to snake_case
     */
    private String toSnakeCase(String input) {
        Matcher matcher = NEW_WORD_BOUNDARY.matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, matcher.group(1) + "_" + matcher.group(2).toLowerCase());
        }
        matcher.appendTail(result);
        return result.toString().toLowerCase();
    }
}
