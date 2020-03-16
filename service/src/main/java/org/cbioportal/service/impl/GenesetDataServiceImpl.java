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

import org.cbioportal.model.GenesetMolecularAlteration;
import org.cbioportal.model.GenesetMolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileSamples;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.GenesetDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenesetDataServiceImpl implements GenesetDataService {

    @Autowired
    private MolecularDataRepository molecularDataRepository;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private SampleListService sampleListService;

    public List<GenesetMolecularData> fetchGenesetData(String molecularProfileId, List<String> sampleIds, List<String> genesetIds)
            throws MolecularProfileNotFoundException {

        //validate (throws exception if profile is not found):
        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
        
        List<GenesetMolecularData> genesetDataList = new ArrayList<>();

        MolecularProfileSamples commaSeparatedSampleIdsOfGeneticProfile = molecularDataRepository
            .getCommaSeparatedSampleIdsOfMolecularProfile(molecularProfileId);
        if (commaSeparatedSampleIdsOfGeneticProfile == null) {
        	//no data, return empty list:
            return genesetDataList;
        }
        List<Integer> internalSampleIds = Arrays.stream(commaSeparatedSampleIdsOfGeneticProfile.getSplitSampleIds())
            .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

        List<Sample> samples;
        if (sampleIds == null) {
            samples = sampleService.getSamplesByInternalIds(internalSampleIds);
        } else {
            List<String> studyIds = new ArrayList<>();
            sampleIds.forEach(s -> studyIds.add(molecularProfile.getCancerStudyIdentifier()));
            samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");
        }

        List<GenesetMolecularAlteration> genesetAlterations = molecularDataRepository.getGenesetMolecularAlterations(molecularProfileId,
                genesetIds, "SUMMARY");

        for (Sample sample : samples) {
            int indexOfSampleId = internalSampleIds.indexOf(sample.getInternalId());
            if (indexOfSampleId != -1) {
                for (GenesetMolecularAlteration genesetAlteration : genesetAlterations) {
                    GenesetMolecularData genesetData = new GenesetMolecularData();
                    genesetData.setMolecularProfileId(molecularProfileId);
                    genesetData.setSampleId(sample.getStableId());
                    genesetData.setPatientId(sample.getPatientStableId());
                    genesetData.setStudyId(sample.getCancerStudyIdentifier());
                    genesetData.setGenesetId(genesetAlteration.getGenesetId());
                    genesetData.setValue(genesetAlteration.getSplitValues()[indexOfSampleId]);
                    genesetDataList.add(genesetData);
                }
            }
        }
        
        return genesetDataList;
    }

    public List<GenesetMolecularData> fetchGenesetData(String geneticProfileId, String sampleListId, List<String> genesetIds) 
            throws MolecularProfileNotFoundException, SampleListNotFoundException {

        //get list of samples for given sampleListId:
        List<String> sampleIds = sampleListService.getAllSampleIdsInSampleList(sampleListId);
        return fetchGenesetData(geneticProfileId, sampleIds, genesetIds);
    }

    @Override
    public List<GenesetMolecularAlteration> getGenesetAlterations(String molecularProfileId, List<String> genesetIds)
        throws MolecularProfileNotFoundException {

        return molecularDataRepository.getGenesetMolecularAlterations(molecularProfileId, genesetIds, "SUMMARY");
    }
}
