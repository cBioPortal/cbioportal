package org.cbioportal.legacy.service;

import java.util.List;
import java.util.function.Consumer;
import org.cbioportal.legacy.model.CopyNumberSeg;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.SampleNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;

public interface CopyNumberSegmentService {

  List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(
      String studyId,
      String sampleId,
      String chromosome,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws SampleNotFoundException, StudyNotFoundException;

  BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(
      String studyId, String sampleId, String chromosome)
      throws SampleNotFoundException, StudyNotFoundException;

  List<CopyNumberSeg> fetchCopyNumberSegments(
      List<String> studyIds, List<String> sampleIds, String chromosome, String projection);

  void streamCopyNumberSegments(
      List<String> studyIds,
      List<String> sampleIds,
      String chromosome,
      String projection,
      Consumer<CopyNumberSeg> consumer);

  BaseMeta fetchMetaCopyNumberSegments(
      List<String> studyIds, List<String> sampleIds, String chromosome);

  List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(
      String studyId, String sampleListId, String chromosome, String projection);
}
