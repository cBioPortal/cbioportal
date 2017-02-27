package org.cbioportal.service;


import java.util.List;
import org.cbioportal.model.MutationalSignature;

public interface MutationalSignatureService {

    List<MutationalSignature> getMutationalSignaturesBySampleIds(String study_id, List<String> sample_ids);
    
    List<MutationalSignature> getMutationalSignatures(String study_id);
}
