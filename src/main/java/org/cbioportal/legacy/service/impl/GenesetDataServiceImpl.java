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

package org.cbioportal.legacy.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.legacy.model.GenesetMolecularAlteration;
import org.cbioportal.legacy.model.GenesetMolecularData;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.persistence.MolecularDataRepository;
import org.cbioportal.legacy.service.GenesetDataService;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.SampleListService;
import org.cbioportal.legacy.service.SampleService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.service.exception.SampleListNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenesetDataServiceImpl implements GenesetDataService {

  @Autowired private MolecularDataRepository molecularDataRepository;
  @Autowired private SampleService sampleService;
  @Autowired private MolecularProfileService molecularProfileService;
  @Autowired private SampleListService sampleListService;

  public List<GenesetMolecularData> fetchGenesetData(
      String molecularProfileId, List<String> sampleIds, List<String> genesetIds)
      throws MolecularProfileNotFoundException {

    // validate (throws exception if profile is not found):
    MolecularProfile molecularProfile =
        molecularProfileService.getMolecularProfile(molecularProfileId);

    List<GenesetMolecularData> genesetDataList = new ArrayList<>();

    List<String> externalSampleIdsOfGeneticProfile =
        molecularDataRepository.getStableSampleIdsOfMolecularProfile(molecularProfileId);
    if (externalSampleIdsOfGeneticProfile == null) {
      // no data, return empty list:
      return genesetDataList;
    }

    List<Sample> samples;
    if (sampleIds == null) {
      sampleIds = externalSampleIdsOfGeneticProfile;
    }
    List<String> studyIds = new ArrayList<>();
    sampleIds.forEach(s -> studyIds.add(molecularProfile.getCancerStudyIdentifier()));
    samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");

    List<GenesetMolecularAlteration> genesetAlterations =
        molecularDataRepository.getGenesetMolecularAlterations(
            molecularProfileId, genesetIds, "SUMMARY");

    for (Sample sample : samples) {
      int indexOfSampleId = externalSampleIdsOfGeneticProfile.indexOf(sample.getStableId());
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

  public List<GenesetMolecularData> fetchGenesetData(
      String geneticProfileId, String sampleListId, List<String> genesetIds)
      throws MolecularProfileNotFoundException, SampleListNotFoundException {

    // get list of samples for given sampleListId:
    List<String> sampleIds = sampleListService.getAllSampleIdsInSampleList(sampleListId);
    return fetchGenesetData(geneticProfileId, sampleIds, genesetIds);
  }

  @Override
  public List<GenesetMolecularAlteration> getGenesetAlterations(
      String molecularProfileId, List<String> genesetIds) throws MolecularProfileNotFoundException {

    return molecularDataRepository.getGenesetMolecularAlterations(
        molecularProfileId, genesetIds, "SUMMARY");
  }
}
