package org.cbioportal.application.file.export;

import com.google.common.collect.Iterables;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TSVUtil {
    public static final String TAB = "\t";

    public static String composeRow(Iterable<String> row) {
       return composeRowOfOptionals(Iterables.transform(row, Optional::ofNullable));
    }

    public static String composeRowOfOptionals(Iterable<Optional<String>> row) {
        return StreamSupport.stream(row.spliterator(), false)
            .map(s -> s.map(string -> string.replace(TAB, "\\t")).orElse("")).collect(Collectors.joining(TAB)) + "\n";
    }
}
