package org.cbioportal.web.parameter;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author kalletlak
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class StudyPageSettings extends PageSettingsData implements Serializable {
    private Object chartSettings;

    public Object getChartSettings() {
        return chartSettings;
    }

    public void setChartSettings(Object chartSettings) {
        this.chartSettings = chartSettings;
    }

}
