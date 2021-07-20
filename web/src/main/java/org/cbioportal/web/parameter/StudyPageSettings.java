package org.cbioportal.web.parameter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.Map;

/**
 * @author kalletlak
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class StudyPageSettings extends PageSettingsData implements Serializable {
    
    private Object chartSettings;
    private Map<String, String> groupColors;

    public Object getChartSettings() {
        return chartSettings;
    }

    public void setChartSettings(Object chartSettings) {
        this.chartSettings = chartSettings;
    }

    public Map<String, String> getGroupColors() {
        return groupColors;
    }

    public void setGroupColors(Map<String, String> groupColors) {
        this.groupColors = groupColors;
    }

}
