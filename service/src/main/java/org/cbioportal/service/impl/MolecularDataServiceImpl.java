package org.cbioportal.service.impl;

import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MolecularDataServiceImpl implements MolecularDataService {

    @Autowired
    private MolecularDataRepository molecularDataRepository;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private SampleListRepository sampleListRepository;

    @Override
    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    public List<GeneMolecularData> getMolecularData(String molecularProfileId, String sampleListId,
                                                    List<Integer> entrezGeneIds, String projection)
        throws MolecularProfileNotFoundException {
        
        validateMolecularProfile(molecularProfileId);
        List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
        if (sampleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return fetchMolecularData(molecularProfileId, sampleIds, entrezGeneIds, projection);
    }

    @Override
    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    public BaseMeta getMetaMolecularData(String molecularProfileId, String sampleListId, List<Integer> entrezGeneIds) 
        throws MolecularProfileNotFoundException {
        
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(getMolecularData(molecularProfileId, sampleListId, entrezGeneIds, "ID").size());
        return baseMeta;
    }

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    public List<GeneMolecularData> fetchMolecularData(String molecularProfileId, List<String> sampleIds,
                                                      List<Integer> entrezGeneIds, String projection) 
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);
        List<GeneMolecularData> molecularDataList = new ArrayList<>();

        String commaSeparatedSampleIdsOfMolecularProfile = molecularDataRepository
            .getCommaSeparatedSampleIdsOfMolecularProfile(molecularProfileId);
        if (commaSeparatedSampleIdsOfMolecularProfile == null) {
            return molecularDataList;
        }
        List<Integer> internalSampleIds = Arrays.stream(commaSeparatedSampleIdsOfMolecularProfile.split(","))
            .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

        List<Sample> samples;
        if (sampleIds == null) {
            samples = sampleService.getSamplesByInternalIds(internalSampleIds);
        } else {
            MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
            List<String> studyIds = new ArrayList<>();
            sampleIds.forEach(s -> studyIds.add(molecularProfile.getCancerStudyIdentifier()));
            samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");
        }

        List<GeneMolecularAlteration> molecularAlterations = molecularDataRepository.getGeneMolecularAlterations(
            molecularProfileId, entrezGeneIds, projection);
        
        for (Sample sample : samples) {
            int indexOfSampleId = internalSampleIds.indexOf(sample.getInternalId());
            if (indexOfSampleId != -1) {
                for (GeneMolecularAlteration molecularAlteration : molecularAlterations) {
                    GeneMolecularData molecularData = new GeneMolecularData();
                    molecularData.setMolecularProfileId(molecularProfileId);
                    molecularData.setSampleId(sample.getStableId());
                    molecularData.setPatientId(sample.getPatientStableId());
                    molecularData.setStudyId(sample.getCancerStudyIdentifier());
                    molecularData.setEntrezGeneId(molecularAlteration.getEntrezGeneId());
                    molecularData.setValue(molecularAlteration.getSplitValues()[indexOfSampleId]);
                    molecularData.setGene(molecularAlteration.getGene());
                    molecularDataList.add(molecularData);
                }
            }
        }
        
        return molecularDataList;
    }

    @Override
    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    public BaseMeta fetchMetaMolecularData(String molecularProfileId, List<String> sampleIds, 
                                           List<Integer> entrezGeneIds) throws MolecularProfileNotFoundException {
        
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(fetchMolecularData(molecularProfileId, sampleIds, entrezGeneIds, "ID").size());
        return baseMeta;
    }

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    public Integer getNumberOfSamplesInMolecularProfile(String molecularProfileId) {

        String commaSeparatedSampleIdsOfMolecularProfile = molecularDataRepository
            .getCommaSeparatedSampleIdsOfMolecularProfile(molecularProfileId);
        if (commaSeparatedSampleIdsOfMolecularProfile == null) {
            return null;
        }
        
        return commaSeparatedSampleIdsOfMolecularProfile.split(",").length;
    }

    private void validateMolecularProfile(String molecularProfileId) throws MolecularProfileNotFoundException {

        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);

        if (molecularProfile.getMolecularAlterationType().equals(MolecularAlterationType.MUTATION_EXTENDED) || 
            molecularProfile.getMolecularAlterationType().equals(MolecularAlterationType.FUSION)) {

            throw new MolecularProfileNotFoundException(molecularProfileId);
        }
    }
}
