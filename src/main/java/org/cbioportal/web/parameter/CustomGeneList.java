package org.cbioportal.web.parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.utils.removeme.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomGeneList extends Session {

    private final Logger LOG = LoggerFactory.getLogger(CustomGeneList.class);
    private CustomGeneListData data;

    @Override
    public void setData(Object data) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.data = mapper.readValue(mapper.writeValueAsString(data), CustomGeneListData.class);
        } catch (IOException e) {
            LOG.error("Error occurred", e);
        }
    }

    @Override
    public CustomGeneListData getData() {
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
