package org.cbioportal.file.export;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TSVUtil {
    public static final String TAB = "\t";

    public static String composeRow(Iterable<String> row) {
        return StreamSupport.stream(row.spliterator(), false)
            .map(s -> s == null ? "" : s.replace(TAB, "\\t")).collect(Collectors.joining(TAB)) + "\n";
    }
}
