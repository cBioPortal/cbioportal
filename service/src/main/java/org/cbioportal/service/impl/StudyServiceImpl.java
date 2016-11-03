package org.cbioportal.service.impl;

import java.util.ArrayList;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudyServiceImpl implements StudyService {

    @Autowired
    private StudyRepository studyRepository;

    @Override
    public List<CancerStudy> getAllStudies(String projection, Integer pageSize, Integer pageNumber,
                                           String sortBy, String direction) {
        if(projection.endsWith("DETAILED")){
            //The result returned is stable_id based. Format it to cancer study based, and assign sample count by analysis type from the stable_id suffix.
            List<CancerStudy> tempResults = studyRepository.getAllStudies(projection, pageSize, pageNumber, sortBy, direction);
            List<CancerStudy> finalResults = new ArrayList<CancerStudy>();
            List<String> cancerStudyIdentifiers = new ArrayList<>();
            CancerStudy tempItem = new CancerStudy();
            //9 possible sample analysis type
            String suffixes[] = {"_all", "_sequenced", "_cna", "_rna_seq_v2_mrna", "_microrna", "_mrna", "_methylation_hm27", "_rppa", "_complete"};
            int suffixIndex = 0;
            for(CancerStudy item : tempResults){
                suffixIndex = -1;
                if(!cancerStudyIdentifiers.contains(item.getCancerStudyIdentifier())){
                    cancerStudyIdentifiers.add(item.getCancerStudyIdentifier());
                    finalResults.add(item);
                }
                for(int i = 0;i < finalResults.size();i++){
                    if(finalResults.get(i).getCancerStudyIdentifier().equals(item.getCancerStudyIdentifier())){
                        tempItem = finalResults.get(i);
                        break;
                    }
                }
                for(int i = 0;i < suffixes.length;i++){
                    if(item.getStableID().endsWith(suffixes[i])){
                        suffixIndex = i;
                        break;
                    }
                }
                switch(suffixIndex){
                    case 0:
                        tempItem.setAllCount(item.getSampleCount());
                        break;
                    case 1:
                        tempItem.setSequencedCount(item.getSampleCount());
                        break;
                    case 2:
                        tempItem.setCnaCount(item.getSampleCount());
                        break;
                    case 3:
                        tempItem.setRna_seq_v2_mrna_count(item.getSampleCount());
                        break;
                    case 4:
                        tempItem.setMicrorna_count(item.getSampleCount());
                        break;
                    case 5:
                        tempItem.setMrna_count(item.getSampleCount());
                        break;
                    case 6:
                        tempItem.setMethylation_hm27_count(item.getSampleCount());
                        break;
                    case 7:
                        tempItem.setRppa_count(item.getSampleCount());
                        break;
                    case 8:
                        tempItem.setComplete_count(item.getSampleCount());
                        break;    
                    default:
                        break;
                }
                //stableId and sampleCount are already used to set analysis type sample count values. remove it from the returned result.
                tempItem.setStableID(null);
                tempItem.setSampleCount(null);
            }
            
            return finalResults;
        }else{
            return studyRepository.getAllStudies(projection, pageSize, pageNumber, sortBy, direction);
        }
    }

    @Override
    public BaseMeta getMetaStudies() {
        return studyRepository.getMetaStudies();
    }

    @Override
    public CancerStudy getStudy(String studyId) throws StudyNotFoundException {

        CancerStudy cancerStudy = studyRepository.getStudy(studyId);
        if (cancerStudy == null) {
            throw new StudyNotFoundException(studyId);
        }

        return cancerStudy;
    }
}
