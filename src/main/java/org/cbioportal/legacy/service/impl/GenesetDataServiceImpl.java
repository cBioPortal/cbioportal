/*
 * Copyright (c) 2016 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.GenesetMolecularAlteration;
import org.cbioportal.legacy.model.GenesetMolecularData;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileSamples;
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

    MolecularProfileSamples commaSeparatedSampleIdsOfGeneticProfile =
        molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(molecularProfileId);
    if (commaSeparatedSampleIdsOfGeneticProfile == null) {
      // no data, return empty list:
      return genesetDataList;
    }
    List<Integer> internalSampleIds =
        Arrays.stream(commaSeparatedSampleIdsOfGeneticProfile.getSplitSampleIds())
            .mapToInt(Integer::parseInt)
            .boxed()
            .collect(Collectors.toList());

    List<Sample> samples;
    if (sampleIds == null) {
      samples = sampleService.getSamplesByInternalIds(internalSampleIds);
    } else {
      List<String> studyIds = new ArrayList<>();
      sampleIds.forEach(s -> studyIds.add(molecularProfile.getCancerStudyIdentifier()));
      samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");
    }

    List<GenesetMolecularAlteration> genesetAlterations =
        molecularDataRepository.getGenesetMolecularAlterations(
            molecularProfileId, genesetIds, "SUMMARY");

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
