package org.mskcc.cbio.maf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import static org.mskcc.cbio.maf.ValueTypeUtil.isDouble;
import static org.mskcc.cbio.maf.ValueTypeUtil.isFloat;
import static org.mskcc.cbio.maf.ValueTypeUtil.isInt;

public class NamespaceColumnParser {

    public static final String NAMESPACE_DELIMITER = ".";
    public static final String NAMESPACE_DELIMITER_REGEX = "\\.";

    private Map<String, Map<String, Integer>> namespaceIndexMap;
    private static ObjectMapper mapper;

    public NamespaceColumnParser(Set<String> namespaces, String[] parts) {
        this.namespaceIndexMap = new HashMap<>();
        this.mapper = new ObjectMapper();
        findNamespaceHeaders(namespaces, parts);
    }

    public Map<String, Map<String, Integer>> getNamespaceColumnIndexMap() {
        return namespaceIndexMap;
    }

    private void findNamespaceHeaders(
        Set<String> namespaces,
        String[] parts
    ) {
        // find required header indices
        for (int i = 0; i < parts.length; i++) {
            String header = parts[i];
            if (namespaces == null || namespaces.isEmpty()) {
                continue;
            }
            int columnIndex = i;
            namespaces
                .stream()
                // Perform a case-insensitive match of namespace in meta file with the column name.
                .filter(namespace -> header.toLowerCase().startsWith(namespace.toLowerCase(
                    Locale.ROOT) + NAMESPACE_DELIMITER))
                .findFirst()
                .ifPresent(namespace -> {
                    String columnName = header.split(NAMESPACE_DELIMITER_REGEX)[1];
                    // For legacy reasons perform lower-case transformation for ASCN column names. 
                    if (namespace.equalsIgnoreCase("ascn")) {
                        columnName = columnName.toLowerCase();
                    }
                    // Key the namespaces with the format (upper-/lowercase) specified in the meta file.
                    Map<String, Integer> nsKeyIndexMap = this.namespaceIndexMap.getOrDefault(namespace, new HashMap<>());
                    nsKeyIndexMap.put(columnName, columnIndex);
                    this.namespaceIndexMap.put(namespace, nsKeyIndexMap);
                });
        }
    }

    public Map<String, Map<String, Object>> parseCustomNamespaces(String[] parts) {
        // extract namespace key-value pairs for json annotation support
        Map<String, Map<String, Object>> recordNamespaceAnnotationJsonMap = new HashMap<>();
        if (this.namespaceIndexMap.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, Map<String, Integer>> nsKeyIndexMap : namespaceIndexMap.entrySet()) {
            String namespace = nsKeyIndexMap.getKey();
            // construct map of the key-value pairs from the record
            Map<String, Object> namespaceKeyValueMappings = new HashMap<>();
            for (Map.Entry<String, Integer> nsKeyIndexPairs : nsKeyIndexMap.getValue().entrySet()) {
                String keyName = nsKeyIndexPairs.getKey();
                Integer keyIndex = nsKeyIndexPairs.getValue();
                String stringValue = TabDelimitedFileUtil.getPartStringAllowEmptyAndNA(keyIndex, parts);
                namespaceKeyValueMappings.put(keyName, parseNamespaceValue(stringValue));
            }
            // update namespace map with the key-value pairs extracted from record
            recordNamespaceAnnotationJsonMap.put(namespace, namespaceKeyValueMappings);
        }
        return recordNamespaceAnnotationJsonMap;
    }

    public static Object parseNamespaceValue(String stringValue) {
        if (stringValue == null || stringValue.isEmpty()) {
            return null;
        } else if (isInt(stringValue)) {
            return Integer.parseInt(stringValue);
        } else if (isFloat(stringValue)) {
            return Float.parseFloat(stringValue);
        } else if (isDouble(stringValue)) {
            return Double.parseDouble(stringValue);
        }
        return stringValue;
    }

    /**
     * Map to string, or return `null` (instead of `"null"`) when null
     */
    public String writeValueAsString(Map<String, Map<String, Object>> namespaces) throws JsonProcessingException {
        if (namespaces == null || namespaces.isEmpty()) {
            return null;
        }
        return NamespaceColumnParser.mapper.writeValueAsString(namespaces);
    }

}
