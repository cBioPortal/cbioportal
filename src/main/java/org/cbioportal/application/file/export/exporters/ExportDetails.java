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
    private final String exportAsStudyId;

    private final Set<String> sampleIds;

    public ExportDetails(String studyId) {
        this.studyId = studyId;
        this.exportAsStudyId = null;
        this.sampleIds = null;
    }

    public ExportDetails(String studyId, String exportAsStudyId) {
        this.studyId = studyId;
        this.exportAsStudyId = exportAsStudyId;
        this.sampleIds = null;
    }

    public ExportDetails(String studyId, String exportAsStudyId, Set<String> sampleIds) {
        this.studyId = studyId;
        this.exportAsStudyId = exportAsStudyId;
        this.sampleIds = sampleIds;
    }

    public String getStudyId() {
        return studyId;
    }

    public String getExportAsStudyId() {
        return exportAsStudyId;
    }

    public Set<String> getSampleIds() {
        return sampleIds;
    }
}
