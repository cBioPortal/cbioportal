package org.cbioportal.web.parameter;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "page", visible = false)
@JsonSubTypes({ @JsonSubTypes.Type(value = StudyPageSettings.class, name = "study_view") })
public abstract class SessionData implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

}
