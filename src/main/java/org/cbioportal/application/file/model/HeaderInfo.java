package org.cbioportal.application.file.model;

import java.util.SequencedSet;

public interface HeaderInfo {
    Iterable<Iterable<String>> getComments();

    SequencedSet<String> getHeader();
}
