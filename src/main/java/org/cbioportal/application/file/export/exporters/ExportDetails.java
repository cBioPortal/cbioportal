package org.cbioportal.application.file.export.exporters;

import java.util.Optional;

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

    public ExportDetails(String studyId) {
        this.studyId = studyId;
        this.exportAsStudyId = null;
    }

    public ExportDetails(String studyId, String exportAsStudyId) {
        this.studyId = studyId;
        this.exportAsStudyId = exportAsStudyId;
    }

    public String getStudyId() {
        return studyId;
    }

    public String getExportAsStudyId() {
        return exportAsStudyId;
    }

}
