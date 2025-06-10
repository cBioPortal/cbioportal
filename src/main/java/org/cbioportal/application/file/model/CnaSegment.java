package org.cbioportal.application.file.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.function.Function;

public class CnaSegment implements TableRow {
  private String sampleId;
  private String chr;
  private Integer start;
  private Integer end;
  private Integer numProbes;
  private Double segmentMean;

  public String getSampleId() {
    return sampleId;
  }

  public void setSampleId(String sampleId) {
    this.sampleId = sampleId;
  }

  public String getChr() {
    return chr;
  }

  public void setChr(String chr) {
    this.chr = chr;
  }

  public Integer getStart() {
    return start;
  }

  public void setStart(Integer start) {
    this.start = start;
  }

  public Integer getEnd() {
    return end;
  }

  public void setEnd(Integer end) {
    this.end = end;
  }

  public Integer getNumProbes() {
    return numProbes;
  }

  public void setNumProbes(Integer numProbes) {
    this.numProbes = numProbes;
  }

  public Double getSegmentMean() {
    return segmentMean;
  }

  public void setSegmentMean(Double segmentMean) {
    this.segmentMean = segmentMean;
  }

  private static final LinkedHashMap<String, Function<CnaSegment, String>> ROW =
      new LinkedHashMap<>();

  static {
    ROW.put("ID", CnaSegment::getSampleId);
    ROW.put("chrom", CnaSegment::getChr);
    ROW.put("loc.start", data -> data.getStart() == null ? null : data.getStart().toString());
    ROW.put("loc.end", data -> data.getEnd() == null ? null : data.getEnd().toString());
    ROW.put(
        "num.mark", data -> data.getNumProbes() == null ? null : data.getNumProbes().toString());
    ROW.put(
        "seg.mean",
        data -> data.getSegmentMean() == null ? null : data.getSegmentMean().toString());
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
