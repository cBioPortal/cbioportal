/*
 * Copyright (c) 2016 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.cbioportal.model.GenesetGeneticAlteration;
import org.cbioportal.model.GenesetGeneticData;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.GeneticDataRepository;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.GenesetDataService;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class GenesetDataServiceImpl implements GenesetDataService {

    @Autowired
    private GeneticDataRepository geneticDataRepository;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private GeneticProfileService geneticProfileService;
    @Autowired
    private SampleListService sampleListService;

    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<GenesetGeneticData> fetchGenesetData(String geneticProfileId, List<String> sampleIds, List<String> genesetIds)
            throws GeneticProfileNotFoundException {

        //validate (throws exception if profile is not found):
        GeneticProfile geneticProfile = geneticProfileService.getGeneticProfile(geneticProfileId);
        
        List<GenesetGeneticData> genesetDataList = new ArrayList<>();

        String commaSeparatedSampleIdsOfGeneticProfile = geneticDataRepository
            .getCommaSeparatedSampleIdsOfGeneticProfile(geneticProfileId);
        if (commaSeparatedSampleIdsOfGeneticProfile == null) {
        	//no data, return empty list:
            return genesetDataList;
        }
        List<Integer> internalSampleIds = Arrays.stream(commaSeparatedSampleIdsOfGeneticProfile.split(","))
            .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

        List<Sample> samples;
        if (sampleIds == null) {
            samples = sampleService.getSamplesByInternalIds(internalSampleIds);
        } else {
            List<String> studyIds = new ArrayList<>();
            sampleIds.forEach(s -> studyIds.add(geneticProfile.getCancerStudyIdentifier()));
            samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");
        }

        List<GenesetGeneticAlteration> genesetAlterations = geneticDataRepository.getGenesetGeneticAlterations(geneticProfileId,
                genesetIds, "SUMMARY");

        for (Sample sample : samples) {
            int indexOfSampleId = internalSampleIds.indexOf(sample.getInternalId());
            if (indexOfSampleId != -1) {
                for (GenesetGeneticAlteration genesetAlteration : genesetAlterations) {
                    GenesetGeneticData genesetData = new GenesetGeneticData();
                    genesetData.setGeneticProfileId(geneticProfileId);
                    genesetData.setSampleId(sample.getStableId());
                    genesetData.setGenesetId(genesetAlteration.getGenesetId());
                    genesetData.setValue(genesetAlteration.getSplitValues()[indexOfSampleId]);
                    genesetDataList.add(genesetData);
                }
            }
        }
        
        return genesetDataList;
    }

    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<GenesetGeneticData> fetchGenesetData(String geneticProfileId, String sampleListId, List<String> genesetIds) 
            throws GeneticProfileNotFoundException, SampleListNotFoundException {

        //get list of samples for given sampleListId:
        List<String> sampleIds = sampleListService.getAllSampleIdsInSampleList(sampleListId);
        return fetchGenesetData(geneticProfileId, sampleIds, genesetIds);
    }
}
