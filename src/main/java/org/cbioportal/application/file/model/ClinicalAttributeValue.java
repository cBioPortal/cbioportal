package org.cbioportal.application.file.model;

import java.util.SequencedMap;

public interface ClinicalAttributeValue {
    SequencedMap<String, String> getKey();
    SequencedMap<String, String> getValue();
}
