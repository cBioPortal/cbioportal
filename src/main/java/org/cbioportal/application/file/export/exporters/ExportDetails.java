package org.cbioportal.application.file.export.exporters;

import java.util.Set;

public class ExportDetails {
    /**
     * The study id to export
     */
    private final String studyId;
    /**
     * The study id to export as. This is used when exporting data for a study that is not the same as the study id
     * e.g. when exporting a Virtual Study
     */
    private String exportWithStudyId;
    private Set<String> sampleIds;
    private String exportWithStudyName;
    private String exportAsStudyDescription;
    private String exportWithStudyPmid;
    private String exportWithStudyTypeOfCancerId;

    public ExportDetails(String studyId) {
        this.studyId = studyId;
    }

    public ExportDetails(String studyId, String exportWithStudyId) {
        this.studyId = studyId;
        this.exportWithStudyId = exportWithStudyId;
    }

    public ExportDetails(String studyId, String exportWithStudyId, Set<String> sampleIds) {
        this.studyId = studyId;
        this.exportWithStudyId = exportWithStudyId;
        this.sampleIds = sampleIds;
    }

    public ExportDetails(String studyId, String exportWithStudyId, Set<String> sampleIds, String exportWithStudyName, String exportAsStudyDescription, String exportWithStudyPmid, String exportWithStudyTypeOfCancerId) {
        this.studyId = studyId;
        this.exportWithStudyId = exportWithStudyId;
        this.sampleIds = sampleIds;
        this.exportWithStudyName = exportWithStudyName;
        this.exportAsStudyDescription = exportAsStudyDescription;
        this.exportWithStudyPmid = exportWithStudyPmid;
        this.exportWithStudyTypeOfCancerId = exportWithStudyTypeOfCancerId;
    }

    public String getStudyId() {
        return studyId;
    }

    public String getExportWithStudyId() {
        return exportWithStudyId;
    }

    public Set<String> getSampleIds() {
        return sampleIds;
    }

    public String getExportWithStudyName() {
        return exportWithStudyName;
    }

    public String getExportAsStudyDescription() {
        return exportAsStudyDescription;
    }

    public String getExportWithStudyPmid() {
        return exportWithStudyPmid;
    }

    public String getExportWithStudyTypeOfCancerId() {
        return exportWithStudyTypeOfCancerId;
    }

}
