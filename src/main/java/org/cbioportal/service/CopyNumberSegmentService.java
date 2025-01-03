package org.cbioportal.service;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.List;

public interface CopyNumberSegmentService {

    /**
     * Retrieves sorted list of copy number segments for a specific sample 
     * within a specific study. This method allows filtering by chromosome and projection.
     *
     * @param studyId   the ID of the study
     * @param sampleId  the ID of the sample
     * @param chromosome the chromosome to filter by
     * @param projection the projection type
     * @param pageSize  the number of records per page
     * @param pageNumber the page number to retrieve
     * @param sortBy    the field to sort the results by
     * @param direction the sort direction (e.g., ASC or DESC)
     * @return a list of copy number segments
     * @throws SampleNotFoundException if the sample is not found
     * @throws StudyNotFoundException  if the study is not found
     */
    List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(
        String studyId, 
        String sampleId, 
        String chromosome, 
        String projection,
        Integer pageSize, 
        Integer pageNumber, 
        String sortBy,
        String direction
    ) throws SampleNotFoundException, StudyNotFoundException;

    /**
     * Retrieves metadata for copy number segments for a specific sample 
     * within a specific study, filtered by chromosome.
     *
     * @param studyId   the ID of the study
     * @param sampleId  the ID of the sample
     * @param chromosome the chromosome to filter by
     * @return the metadata for the copy number segments
     * @throws SampleNotFoundException if the sample is not found
     * @throws StudyNotFoundException  if the study is not found
     */
    BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(
        String studyId, 
        String sampleId, 
        String chromosome
    ) throws SampleNotFoundException, StudyNotFoundException;

    /**
     * Fetches copy number segments for multiple studies and samples, 
     * filtered by chromosome and projection.
     *
     * @param studyIds   a list of study IDs
     * @param sampleIds  a list of sample IDs
     * @param chromosome the chromosome to filter by
     * @param projection the projection type
     * @return a list of copy number segments
     */
    List<CopyNumberSeg> fetchCopyNumberSegments(
        List<String> studyIds, 
        List<String> sampleIds, 
        String chromosome, 
        String projection
    );

    /**
     * Fetches metadata for copy number segments for multiple studies and samples, 
     * filtered by chromosome.
     *
     * @param studyIds   a list of study IDs
     * @param sampleIds  a list of sample IDs
     * @param chromosome the chromosome to filter by
     * @return the metadata for the copy number segments
     */
    BaseMeta fetchMetaCopyNumberSegments(
        List<String> studyIds, 
        List<String> sampleIds, 
        String chromosome
    );

    /**
     * Retrieves copy number segments for a list of samples within a study, 
     * filtered by chromosome and projection.
     *
     * @param studyId     the ID of the study
     * @param sampleListId the ID of the sample list
     * @param chromosome  the chromosome to filter by
     * @param projection  the projection type
     * @return a list of copy number segments
     */
    List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(
        String studyId, 
        String sampleListId, 
        String chromosome, 
        String projection
    );
}
