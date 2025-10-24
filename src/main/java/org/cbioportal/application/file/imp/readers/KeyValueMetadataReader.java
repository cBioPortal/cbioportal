package org.cbioportal.application.file.imp.readers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

/**
 * Deserializes key-value metadata to cBioPortal metadata format e.g. meta_study.txt
 */
public class KeyValueMetadataReader {

    private final Reader reader;

    /**
     * @param reader - the reader to read the key-value metadata to e.g. StringReader
     */
    public KeyValueMetadataReader(Reader reader) {
        this.reader = reader;
    }

    private static Map.Entry<String, String> parseKeyValueLine(String line) {
        // Strip trailing newline if present
        if (line.endsWith("\n")) {
            line = line.substring(0, line.length() - 1);
        }

        // Find the first ": "
        int sepIdx = line.indexOf(": ");
        if (sepIdx < 0) {
            throw new IllegalArgumentException("Invalid key-value line: " + line);
        }

        String key = line.substring(0, sepIdx);
        String rawValue = line.substring(sepIdx + 2);

        // Reverse the escaped newline re-encoding
        String value = rawValue.replace("\\n", "\n");

        return new AbstractMap.SimpleEntry<>(key, value);
        /** Reads a stream of key-value pairs from the reader */
    }

    public SequencedMap<String, String> readMetadata() throws IOException {
        var result = new LinkedHashMap<String, String>();
        try(BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                var pairEntry = parseKeyValueLine(line);
                result.put(pairEntry.getKey(), pairEntry.getValue());
            }
        }
        return result;
    }
}
