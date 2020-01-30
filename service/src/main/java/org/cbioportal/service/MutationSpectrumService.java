package org.cbioportal.service;

import java.util.List;
import org.cbioportal.model.MutationSpectrum;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

public interface MutationSpectrumService {
    List<MutationSpectrum> getMutationSpectrums(
        String molecularProfileId,
        String sampleListId
    )
        throws MolecularProfileNotFoundException;

    List<MutationSpectrum> fetchMutationSpectrums(
        String molecularProfileId,
        List<String> sampleIds
    )
        throws MolecularProfileNotFoundException;
}
