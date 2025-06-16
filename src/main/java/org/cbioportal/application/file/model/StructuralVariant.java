package org.cbioportal.application.file.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.function.Function;

public class StructuralVariant implements TableRow {
  private String sampleId;
  private Integer site1EntrezGeneId;
  private String site1HugoSymbol;
  private String site1EnsemblTranscriptId;
  private String site1Chromosome;
  private String site1Region;
  private Integer site1RegionNumber;
  private String site1Contig;
  private Integer site1Position;
  private String site1Description;
  private Integer site2EntrezGeneId;
  private String site2HugoSymbol;
  private String site2EnsemblTranscriptId;
  private String site2Chromosome;
  private String site2Region;
  private Integer site2RegionNumber;
  private String site2Contig;
  private Integer site2Position;
  private String site2Description;
  private String site2EffectOnFrame;
  private String ncbiBuild;
  private String dnaSupport;
  private String rnaSupport;
  private Integer normalReadCount;
  private Integer tumorReadCount;
  private Integer normalVariantCount;
  private Integer tumorVariantCount;
  private Integer normalPairedEndReadCount;
  private Integer tumorPairedEndReadCount;
  private Integer normalSplitReadCount;
  private Integer tumorSplitReadCount;
  private String annotation;
  private String breakpointType;
  private String connectionType;
  private String eventInfo;
  private String structuralVariantClass;
  private Integer length;
  private String comments;
  private String svStatus;

  public String getSampleId() {
    return sampleId;
  }

  public void setSampleId(String sampleId) {
    this.sampleId = sampleId;
  }

  public String getSite1HugoSymbol() {
    return site1HugoSymbol;
  }

  public void setSite1HugoSymbol(String site1HugoSymbol) {
    this.site1HugoSymbol = site1HugoSymbol;
  }

  public String getSite2HugoSymbol() {
    return site2HugoSymbol;
  }

  public void setSite2HugoSymbol(String site2HugoSymbol) {
    this.site2HugoSymbol = site2HugoSymbol;
  }

  public Integer getSite1EntrezGeneId() {
    return site1EntrezGeneId;
  }

  public void setSite1EntrezGeneId(Integer site1EntrezGeneId) {
    this.site1EntrezGeneId = site1EntrezGeneId;
  }

  public String getSite1EnsemblTranscriptId() {
    return site1EnsemblTranscriptId;
  }

  public void setSite1EnsemblTranscriptId(String site1EnsemblTranscriptId) {
    this.site1EnsemblTranscriptId = site1EnsemblTranscriptId;
  }

  public String getSite1Chromosome() {
    return site1Chromosome;
  }

  public void setSite1Chromosome(String site1Chromosome) {
    this.site1Chromosome = site1Chromosome;
  }

  public String getSite1Region() {
    return site1Region;
  }

  public void setSite1Region(String site1Region) {
    this.site1Region = site1Region;
  }

  public Integer getSite1RegionNumber() {
    return site1RegionNumber;
  }

  public void setSite1RegionNumber(Integer site1RegionNumber) {
    this.site1RegionNumber = site1RegionNumber;
  }

  public String getSite1Contig() {
    return site1Contig;
  }

  public void setSite1Contig(String site1Contig) {
    this.site1Contig = site1Contig;
  }

  public Integer getSite1Position() {
    return site1Position;
  }

  public void setSite1Position(Integer site1Position) {
    this.site1Position = site1Position;
  }

  public String getSite1Description() {
    return site1Description;
  }

  public void setSite1Description(String site1Description) {
    this.site1Description = site1Description;
  }

  public Integer getSite2EntrezGeneId() {
    return site2EntrezGeneId;
  }

  public void setSite2EntrezGeneId(Integer site2EntrezGeneId) {
    this.site2EntrezGeneId = site2EntrezGeneId;
  }

  public String getSite2EnsemblTranscriptId() {
    return site2EnsemblTranscriptId;
  }

  public void setSite2EnsemblTranscriptId(String site2EnsemblTranscriptId) {
    this.site2EnsemblTranscriptId = site2EnsemblTranscriptId;
  }

  public String getSite2Chromosome() {
    return site2Chromosome;
  }

  public void setSite2Chromosome(String site2Chromosome) {
    this.site2Chromosome = site2Chromosome;
  }

  public String getSite2Region() {
    return site2Region;
  }

  public void setSite2Region(String site2Region) {
    this.site2Region = site2Region;
  }

  public Integer getSite2RegionNumber() {
    return site2RegionNumber;
  }

  public void setSite2RegionNumber(Integer site2RegionNumber) {
    this.site2RegionNumber = site2RegionNumber;
  }

  public String getSite2Contig() {
    return site2Contig;
  }

  public void setSite2Contig(String site2Contig) {
    this.site2Contig = site2Contig;
  }

  public Integer getSite2Position() {
    return site2Position;
  }

  public void setSite2Position(Integer site2Position) {
    this.site2Position = site2Position;
  }

  public String getSite2Description() {
    return site2Description;
  }

  public void setSite2Description(String site2Description) {
    this.site2Description = site2Description;
  }

  public String getSite2EffectOnFrame() {
    return site2EffectOnFrame;
  }

  public void setSite2EffectOnFrame(String site2EffectOnFrame) {
    this.site2EffectOnFrame = site2EffectOnFrame;
  }

  public String getNcbiBuild() {
    return ncbiBuild;
  }

  public void setNcbiBuild(String ncbiBuild) {
    this.ncbiBuild = ncbiBuild;
  }

  public String getDnaSupport() {
    return dnaSupport;
  }

  public void setDnaSupport(String dnaSupport) {
    this.dnaSupport = dnaSupport;
  }

  public String getRnaSupport() {
    return rnaSupport;
  }

  public void setRnaSupport(String rnaSupport) {
    this.rnaSupport = rnaSupport;
  }

  public Integer getNormalReadCount() {
    return normalReadCount;
  }

  public void setNormalReadCount(Integer normalReadCount) {
    this.normalReadCount = normalReadCount;
  }

  public Integer getTumorReadCount() {
    return tumorReadCount;
  }

  public void setTumorReadCount(Integer tumorReadCount) {
    this.tumorReadCount = tumorReadCount;
  }

  public Integer getNormalVariantCount() {
    return normalVariantCount;
  }

  public void setNormalVariantCount(Integer normalVariantCount) {
    this.normalVariantCount = normalVariantCount;
  }

  public Integer getTumorVariantCount() {
    return tumorVariantCount;
  }

  public void setTumorVariantCount(Integer tumorVariantCount) {
    this.tumorVariantCount = tumorVariantCount;
  }

  public Integer getNormalPairedEndReadCount() {
    return normalPairedEndReadCount;
  }

  public void setNormalPairedEndReadCount(Integer normalPairedEndReadCount) {
    this.normalPairedEndReadCount = normalPairedEndReadCount;
  }

  public Integer getTumorPairedEndReadCount() {
    return tumorPairedEndReadCount;
  }

  public void setTumorPairedEndReadCount(Integer tumorPairedEndReadCount) {
    this.tumorPairedEndReadCount = tumorPairedEndReadCount;
  }

  public Integer getNormalSplitReadCount() {
    return normalSplitReadCount;
  }

  public void setNormalSplitReadCount(Integer normalSplitReadCount) {
    this.normalSplitReadCount = normalSplitReadCount;
  }

  public Integer getTumorSplitReadCount() {
    return tumorSplitReadCount;
  }

  public void setTumorSplitReadCount(Integer tumorSplitReadCount) {
    this.tumorSplitReadCount = tumorSplitReadCount;
  }

  public String getAnnotation() {
    return annotation;
  }

  public void setAnnotation(String annotation) {
    this.annotation = annotation;
  }

  public String getBreakpointType() {
    return breakpointType;
  }

  public void setBreakpointType(String breakpointType) {
    this.breakpointType = breakpointType;
  }

  public String getConnectionType() {
    return connectionType;
  }

  public void setConnectionType(String connectionType) {
    this.connectionType = connectionType;
  }

  public String getEventInfo() {
    return eventInfo;
  }

  public void setEventInfo(String eventInfo) {
    this.eventInfo = eventInfo;
  }

  public String getStructuralVariantClass() {
    return structuralVariantClass;
  }

  public void setStructuralVariantClass(String structuralVariantClass) {
    this.structuralVariantClass = structuralVariantClass;
  }

  public Integer getLength() {
    return length;
  }

  public void setLength(Integer length) {
    this.length = length;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public String getSvStatus() {
    return svStatus;
  }

  public void setSvStatus(String svStatus) {
    this.svStatus = svStatus;
  }

  private static final LinkedHashMap<String, Function<StructuralVariant, String>> ROW =
      new LinkedHashMap<>();

  static {
    ROW.put("Sample_Id", StructuralVariant::getSampleId);
    ROW.put(
        "Site1_Entrez_Gene_Id",
        data ->
            data.getSite1EntrezGeneId() == null || data.getSite1EntrezGeneId() < 0
                ? null
                : data.getSite1EntrezGeneId().toString());
    ROW.put("Site1_Hugo_Symbol", StructuralVariant::getSite1HugoSymbol);
    ROW.put("Site1_Ensembl_Transcript_Id", StructuralVariant::getSite1EnsemblTranscriptId);
    ROW.put(
        "Site1_Region_Number",
        data ->
            data.getSite1RegionNumber() == null ? null : data.getSite1RegionNumber().toString());
    ROW.put("Site1_Chromosome", StructuralVariant::getSite1Chromosome);
    ROW.put(
        "Site1_Position",
        data -> data.getSite1Position() == null ? null : data.getSite1Position().toString());
    ROW.put("Site1_Region", StructuralVariant::getSite1Region);
    ROW.put("Site1_Description", StructuralVariant::getSite1Description);
    ROW.put(
        "Site2_Entrez_Gene_Id",
        data ->
            data.getSite2EntrezGeneId() == null || data.getSite2EntrezGeneId() < 0
                ? null
                : data.getSite2EntrezGeneId().toString());
    ROW.put("Site2_Hugo_Symbol", StructuralVariant::getSite2HugoSymbol);
    ROW.put("Site2_Ensembl_Transcript_Id", StructuralVariant::getSite2EnsemblTranscriptId);
    ROW.put(
        "Site2_Region_Number",
        data ->
            data.getSite2RegionNumber() == null ? null : data.getSite2RegionNumber().toString());
    ROW.put("Site2_Chromosome", StructuralVariant::getSite2Chromosome);
    ROW.put(
        "Site2_Position",
        data -> data.getSite2Position() == null ? null : data.getSite2Position().toString());
    ROW.put("Site2_Contig", StructuralVariant::getSite2Contig);
    ROW.put("Site2_Region", StructuralVariant::getSite2Region);
    ROW.put("Site2_Description", StructuralVariant::getSite2Description);
    ROW.put("Site2_Effect_On_Frame", StructuralVariant::getSite2EffectOnFrame);
    ROW.put("NCBI_Build", StructuralVariant::getNcbiBuild);
    ROW.put("DNA_Support", StructuralVariant::getDnaSupport);
    ROW.put("RNA_Support", StructuralVariant::getRnaSupport);
    ROW.put(
        "Normal_Read_Count",
        data -> data.getNormalReadCount() == null ? null : data.getNormalReadCount().toString());
    ROW.put(
        "Tumor_Read_Count",
        data -> data.getTumorReadCount() == null ? null : data.getTumorReadCount().toString());
    ROW.put(
        "Normal_Variant_Count",
        data ->
            data.getNormalVariantCount() == null ? null : data.getNormalVariantCount().toString());
    ROW.put(
        "Tumor_Variant_Count",
        data ->
            data.getTumorVariantCount() == null ? null : data.getTumorVariantCount().toString());
    ROW.put(
        "Normal_Paired_End_Read_Count",
        data ->
            data.getNormalPairedEndReadCount() == null
                ? null
                : data.getNormalPairedEndReadCount().toString());
    ROW.put(
        "Tumor_Paired_End_Read_Count",
        data ->
            data.getTumorPairedEndReadCount() == null
                ? null
                : data.getTumorPairedEndReadCount().toString());
    ROW.put(
        "Normal_Split_Read_Count",
        data ->
            data.getNormalSplitReadCount() == null
                ? null
                : data.getNormalSplitReadCount().toString());
    ROW.put(
        "Tumor_Split_Read_Count",
        data ->
            data.getTumorSplitReadCount() == null
                ? null
                : data.getTumorSplitReadCount().toString());
    ROW.put("Annotation", StructuralVariant::getAnnotation);
    ROW.put("Breakpoint_Type", StructuralVariant::getBreakpointType);
    ROW.put("Connection_Type", StructuralVariant::getConnectionType);
    ROW.put("Event_Info", StructuralVariant::getEventInfo);
    ROW.put("Class", StructuralVariant::getStructuralVariantClass);
    ROW.put("SV_Length", data -> data.getLength() == null ? null : data.getLength().toString());
    ROW.put("Comments", StructuralVariant::getComments);
    ROW.put("SV_Status", StructuralVariant::getSvStatus);
  }

  public static SequencedSet<String> getHeader() {
    return new LinkedHashSet<>(ROW.sequencedKeySet());
  }

  @Override
  public SequencedMap<String, String> toRow() {
    LinkedHashMap<String, String> row = new LinkedHashMap<>();
    ROW.sequencedEntrySet()
        .forEach(
            entry -> {
              String value = entry.getValue().apply(this);
              row.put(entry.getKey(), value);
            });
    return row;
  }
}
