package org.cbioportal.domain.sample;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.util.List;

/**
 * Represents a list of samples.
 */
@Data
public class SampleList {
    private Integer listId;
    private String stableId;
    private String category;
    private String name;
    private String description;
    private String cancerStudyStableId;
    private List<String> sampleStableIds;
}
