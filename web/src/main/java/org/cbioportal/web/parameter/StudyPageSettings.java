package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.NotNull;
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
public class StudyPageSettings implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    static class ChartSetting implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        static class Layout implements Serializable {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

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

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(Include.NON_NULL)
        static class PatientSampleIdentifier extends SampleIdentifier implements Serializable {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;
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

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(Include.NON_NULL)
        static class Group implements Serializable {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

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
        private List<Group> groups;
        private Layout layout;
        private Boolean patientAttribute;

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

        public List<Group> getGroups() {
            return groups;
        }

        public void setGroups(List<Group> groups) {
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

    }

    @NotNull
    private SessionPage page;

    @NotNull
    private List<ChartSetting> chartSettings = new ArrayList<ChartSetting>();

    private String owner = "anonymous";

    @NotNull
    private Set<String> origin = new HashSet<>();

    private Long created = System.currentTimeMillis();

    private Long lastUpdated = System.currentTimeMillis();

    @NotNull
    private Set<String> users = new HashSet<>();

    public SessionPage getPage() {
        return page;
    }

    public void setPage(SessionPage page) {
        this.page = page;
    }

    public List<ChartSetting> getChartSettings() {
        return chartSettings;
    }

    public void setChartSettings(List<ChartSetting> chartSettings) {
        this.chartSettings = chartSettings;
    }

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

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }

}