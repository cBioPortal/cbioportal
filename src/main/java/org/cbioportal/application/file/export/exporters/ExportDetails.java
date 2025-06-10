package org.cbioportal.application.file.export.exporters;

import java.util.Objects;
import java.util.Set;

public class ExportDetails {
  /** The study id to export */
  private final String studyId;

  /**
   * The study id to export as. This is used when exporting data for a study that is not the same as
   * the study id e.g. when exporting a Virtual Study
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

  public ExportDetails(
      String studyId,
      String exportWithStudyId,
      Set<String> sampleIds,
      String exportWithStudyName,
      String exportAsStudyDescription,
      String exportWithStudyPmid,
      String exportWithStudyTypeOfCancerId) {
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

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ExportDetails that = (ExportDetails) o;
    return Objects.equals(studyId, that.studyId)
        && Objects.equals(exportWithStudyId, that.exportWithStudyId)
        && Objects.equals(sampleIds, that.sampleIds)
        && Objects.equals(exportWithStudyName, that.exportWithStudyName)
        && Objects.equals(exportAsStudyDescription, that.exportAsStudyDescription)
        && Objects.equals(exportWithStudyPmid, that.exportWithStudyPmid)
        && Objects.equals(exportWithStudyTypeOfCancerId, that.exportWithStudyTypeOfCancerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        studyId,
        exportWithStudyId,
        sampleIds,
        exportWithStudyName,
        exportAsStudyDescription,
        exportWithStudyPmid,
        exportWithStudyTypeOfCancerId);
  }
}
