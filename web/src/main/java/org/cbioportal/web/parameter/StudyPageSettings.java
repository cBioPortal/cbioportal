package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.Size;

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

    @JsonInclude(Include.NON_NULL)
    static class ChartSetting implements Serializable {

        static class Layout implements Serializable {

            private Integer x;
            private Integer y;
            private Integer w;
            private Integer h;

            public Integer getX() {
                return x;
            }

            public void setX(Integer x) {
                this.x = x;
            }

            public Integer getY() {
                return y;
            }

            public void setY(Integer y) {
                this.y = y;
            }

            public Integer getW() {
                return w;
            }

            public void setW(Integer w) {
                this.w = w;
            }

            public Integer getH() {
                return h;
            }

            public void setH(Integer h) {
                this.h = h;
            }
        }

        @JsonInclude(Include.NON_NULL)
        static class PatientSampleIdentifier extends SampleIdentifier implements Serializable {

            private String patientId;

            public String getPatientId() {
                return patientId;
            }

            public void setPatientId(String patientId) {
                this.patientId = patientId;
            }

            @Override
            public boolean equals(Object o) {
                if (o == this)
                    return true;
                if (!(o instanceof PatientSampleIdentifier)) {
                    return false;
                }
                PatientSampleIdentifier patientSampleIdentifier = (PatientSampleIdentifier) o;
                return Objects.equals(getSampleId(), patientSampleIdentifier.getSampleId())
                        && Objects.equals(getPatientId(), patientSampleIdentifier.getPatientId())
                        && Objects.equals(getStudyId(), patientSampleIdentifier.getStudyId());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getSampleId(), getPatientId(), getStudyId());
            }
        }

        @JsonInclude(Include.NON_NULL)
        static class CustomChartGroup implements Serializable {

            @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
            private List<PatientSampleIdentifier> sampleIdentifiers;

            private String name;

            public List<PatientSampleIdentifier> getSampleIdentifiers() {
                return sampleIdentifiers;
            }

            public void setSampleIdentifiers(List<PatientSampleIdentifier> sampleIdentifiers) {
                this.sampleIdentifiers = sampleIdentifiers;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

        }

        private String id;
        private String name;
        private ChartType chartType;
        private List<CustomChartGroup> groups;
        private Layout layout;
        private Boolean patientAttribute;
        private Boolean filterByCancerGenes;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ChartType getChartType() {
            return chartType;
        }

        public void setChartType(ChartType chartType) {
            this.chartType = chartType;
        }

        public List<CustomChartGroup> getGroups() {
            return groups;
        }

        public void setGroups(List<CustomChartGroup> groups) {
            this.groups = groups;
        }

        public Layout getLayout() {
            return layout;
        }

        public void setLayout(Layout layout) {
            this.layout = layout;
        }

        public Boolean getPatientAttribute() {
            return patientAttribute;
        }

        public void setPatientAttribute(Boolean patientAttribute) {
            this.patientAttribute = patientAttribute;
        }

        public Boolean getFilterByCancerGenes() {
            return filterByCancerGenes;
        }

        public void setFilterByCancerGenes(Boolean filterByCancerGenes) {
            this.filterByCancerGenes = filterByCancerGenes;
        }

    }

    private List<ChartSetting> chartSettings = new ArrayList<ChartSetting>();
    private String owner = "anonymous";
    private Set<String> origin = new HashSet<>();
    private Long created = System.currentTimeMillis();
    private Long lastUpdated = System.currentTimeMillis();

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Set<String> getOrigin() {
        return origin;
    }

    public void setOrigin(Set<String> origin) {
        this.origin = origin;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<ChartSetting> getChartSettings() {
        return chartSettings;
    }

    public void setChartSettings(List<ChartSetting> chartSettings) {
        this.chartSettings = chartSettings;
    }

}
