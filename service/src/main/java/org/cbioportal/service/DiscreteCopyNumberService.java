package org.cbioportal.service;

import java.util.List;
import org.cbioportal.model.CopyNumberCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

public interface DiscreteCopyNumberService {
    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMolecularProfileBySampleListId(
        String molecularProfileId,
        String sampleListId,
        List<Integer> entrezGeneIds,
        List<Integer> alterationTypes,
        String projection
    )
        throws MolecularProfileNotFoundException;

    BaseMeta getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(
        String molecularProfileId,
        String sampleListId,
        List<Integer> entrezGeneIds,
        List<Integer> alterationTypes
    )
        throws MolecularProfileNotFoundException;

    List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInMolecularProfile(
        String molecularProfileId,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        List<Integer> alterationTypes,
        String projection
    )
        throws MolecularProfileNotFoundException;

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfiles(
        List<String> molecularProfileIds,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        List<Integer> alterationTypes,
        String projection
    );

    BaseMeta fetchMetaDiscreteCopyNumbersInMolecularProfile(
        String molecularProfileId,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        List<Integer> alterationTypes
    )
        throws MolecularProfileNotFoundException;

    List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(
        String molecularProfileId,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        List<Integer> alterations
    )
        throws MolecularProfileNotFoundException;

    List<CopyNumberCountByGene> getSampleCountInMultipleMolecularProfiles(
        List<String> molecularProfileIds,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        List<Integer> alterations,
        boolean includeFrequency
    );

    List<CopyNumberCountByGene> getPatientCountInMultipleMolecularProfiles(
        List<String> molecularProfileIds,
        List<String> patientIds,
        List<Integer> entrezGeneIds,
        List<Integer> alterations,
        boolean includeFrequency
    );

    List<CopyNumberCount> fetchCopyNumberCounts(
        String molecularProfileId,
        List<Integer> entrezGeneIds,
        List<Integer> alterations
    )
        throws MolecularProfileNotFoundException;
}
