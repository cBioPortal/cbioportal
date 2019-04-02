package org.cbioportal.web.parameter;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonInclude(Include.NON_NULL)
public class Session {

    private String id;
    private String source;
    private SessionType type;
    
    //replace Object with SessionData once model for main_session and comparison_session are finalized
    private Object data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getData() {
        return data;
    }
    
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", visible = true)
    @JsonSubTypes({ @Type(value = StudyPageSettings.class, name = "settings"),
            @Type(value = HashMap.class, name = "main_session"),
            @Type(value = HashMap.class, name = "comparison_session"),
            @Type(value = VirtualStudyData.class, name = "virtual_study"),
            @Type(value = VirtualStudyData.class, name = "group") })
    public void setData(Object data) {
        this.data = data;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public SessionType getType() {
        return type;
    }

    public void setType(SessionType type) {
        this.type = type;
    }

}
