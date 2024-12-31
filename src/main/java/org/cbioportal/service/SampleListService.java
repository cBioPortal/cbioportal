package org.cbioportal.service;

import org.cbioportal.model.SampleList;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.List;

public interface SampleListService {

        /**
         * Retrieves all sample lists with optional pagination, sorting, and projection settings.
         *
         * @param projection the projection type to specify the fields to include
         * @param pageSize   the number of sample lists to return per page
         * @param pageNumber the page number to retrieve
         * @param sortBy     the field by which to sort the results
         * @param direction  the sort direction (e.g., ASC or DESC)
         * @return a list of sample lists
         */
        List<SampleList> getAllSampleLists(
            String projection,
            Integer pageSize,
            Integer pageNumber,
            String sortBy,
            String direction
        );
    
        /**
         * Retrieves metadata for all sample lists.
         *
         * @return metadata for all sample lists
         */
        BaseMeta getMetaSampleLists();
    
        /**
         * Retrieves a specific sample list by its ID.
         *
         * @param sampleListId the ID of the sample list
         * @return the sample list
         * @throws SampleListNotFoundException if the sample list is not found
         */
        SampleList getSampleList(String sampleListId) throws SampleListNotFoundException;
    
        /**
         * Retrieves all sample lists within a specific study.
         * Supports pagination, sorting, and projection settings.
         *
         * @param studyId     the ID of the study
         * @param projection  the projection type to specify the fields to include
         * @param pageSize    the number of sample lists to return per page
         * @param pageNumber  the page number to retrieve
         * @param sortBy      the field by which to sort the results
         * @param direction   the sort direction (e.g., ASC or DESC)
         * @return a list of sample lists within the study
         * @throws StudyNotFoundException if the study is not found
         */
        List<SampleList> getAllSampleListsInStudy(
            String studyId,
            String projection,
            Integer pageSize,
            Integer pageNumber,
            String sortBy,
            String direction
        ) throws StudyNotFoundException;
    
        /**
         * Retrieves metadata for all sample lists within a specific study.
         *
         * @param studyId the ID of the study
         * @return metadata for the sample lists in the study
         * @throws StudyNotFoundException if the study is not found
         */
        BaseMeta getMetaSampleListsInStudy(String studyId) throws StudyNotFoundException;
    
        /**
         * Retrieves all sample IDs associated with a specific sample list.
         *
         * @param sampleListId the ID of the sample list
         * @return a list of sample IDs
         * @throws SampleListNotFoundException if the sample list is not found
         */
        List<String> getAllSampleIdsInSampleList(String sampleListId) throws SampleListNotFoundException;
    
        /**
         * Fetches multiple sample lists by their IDs with optional projection settings.
         *
         * @param sampleListIds a list of sample list IDs
         * @param projection    the projection type to specify the fields to include
         * @return a list of sample lists
         */
        List<SampleList> fetchSampleLists(
            List<String> sampleListIds,
            String projection
        );
    
        /**
         * Retrieves all sample lists across multiple studies with optional projection settings.
         *
         * @param studyIds   a list of study IDs
         * @param projection the projection type to specify the fields to include
         * @return a list of sample lists across the specified studies
         */
        List<SampleList> getAllSampleListsInStudies(
            List<String> studyIds,
            String projection
        );
    }
