package org.cbioportal.web.mixin.summary;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public class CancerStudySummaryMixin {

    @JsonIgnore
    private Integer cancerStudyId;
    private String cancerStudyIdentifier;
    private String typeOfCancerId;
    private String name;
    private String shortName;
    private String description;
    private Boolean publicStudy;
    private String pmid;
    private String citation;
    private String groups;
    private Integer status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date importDate;
}
