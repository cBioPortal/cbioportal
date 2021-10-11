package org.cbioportal.web.parameter;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.utils.removeme.Session;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualStudy extends Session {

    private final Log LOG = LogFactory.getLog(VirtualStudy.class);
    private VirtualStudyData data;

    @Override
    public void setData(Object data) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.data = mapper.readValue(mapper.writeValueAsString(data), VirtualStudyData.class);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    @Override
    public VirtualStudyData getData() {
        return data;
    }

    @JsonIgnore
    @Override
    public String getSource() {
        return super.getSource();
    }

    @JsonIgnore
    @Override
    public SessionType getType() {
        return super.getType();
    }

}
