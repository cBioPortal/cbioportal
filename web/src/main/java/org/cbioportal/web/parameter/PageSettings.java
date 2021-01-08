package org.cbioportal.web.parameter;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cbioportal.session_service.domain.Session;
import org.cbioportal.session_service.domain.SessionType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PageSettings extends Session {

    private final Log LOG = LogFactory.getLog(PageSettings.class);
    private PageSettingsData data;

    @Override
    public void setData(Object data) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.data = mapper.readValue(mapper.writeValueAsString(data), PageSettingsData.class);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    @Override
    public PageSettingsData getData() {
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
