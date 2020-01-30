package org.mskcc.cbio.portal.service;

import java.util.List;
import org.mskcc.cbio.portal.model.MutationalSignature;

public interface MutationalSignatureService {
    List<MutationalSignature> getMutationalSignaturesBySampleIds(
        String study_id,
        List<String> sample_ids
    );

    List<MutationalSignature> getMutationalSignatures(String study_id);
}
