package org.cbioportal.web.parameter;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cbioportal.session_service.domain.Session;

import com.fasterxml.jackson.databind.ObjectMapper;

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

}
