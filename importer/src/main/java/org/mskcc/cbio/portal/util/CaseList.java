package org.mskcc.cbio.portal.util;

import java.util.*;

public class CaseList {

    private final String stableId;
    private final String cancerStudyIdentifier;
    private final String name;
    private final String description;

    private final String category;
    private final List<String> sampleIds;

    CaseList(String stableId, String cancerStudyIdentifier, String name, String description, String category, List<String> sampleIds) {
        this.stableId = stableId;
        this.cancerStudyIdentifier = cancerStudyIdentifier;
        this.name = name;
        this.description = description;
        this.category = category;
        this.sampleIds = sampleIds;
    }

    public String getStableId() {
        return stableId;
    }

    public String getCancerStudyIdentifier() {
        return cancerStudyIdentifier;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getSampleIds() {
        return sampleIds;
    }

}
