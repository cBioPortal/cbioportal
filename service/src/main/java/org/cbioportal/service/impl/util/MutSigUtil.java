package org.cbioportal.service.impl.util;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoMutSig;
import org.mskcc.cbio.portal.model.MutSig;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MutSigUtil {

    public Map<Long,Double> getMutSig(int cancerStudyId) throws DaoException {
        Map<Long,Double> mapGeneQvalue;
        Map<Integer,Map<Long,Double>> mutSigMap = new HashMap<>();

        mapGeneQvalue = mutSigMap.get(cancerStudyId);
        if (mapGeneQvalue == null) {
            mapGeneQvalue = new HashMap<>();
            mutSigMap.put(cancerStudyId, mapGeneQvalue);
            for (MutSig ms : DaoMutSig.getAllMutSig(cancerStudyId)) {
                double qvalue = ms.getqValue();
                mapGeneQvalue.put(ms.getCanonicalGene().getEntrezGeneId(),
                        qvalue);
            }
        }

        return mapGeneQvalue;
    }
}
