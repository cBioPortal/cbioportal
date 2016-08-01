package org.cbioportal.service.impl.util;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoMutSig;
import org.mskcc.cbio.portal.model.MutSig;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MutSigUtil {

    public Map<Long, Double> getMutSig(int cancerStudyId) throws DaoException {

        Map<Long, Double> mapGeneQvalue = new HashMap<>();

        for (MutSig ms : DaoMutSig.getAllMutSig(cancerStudyId)) {
            mapGeneQvalue.put(ms.getCanonicalGene().getEntrezGeneId(), (double) ms.getqValue());
        }
        return mapGeneQvalue;
    }
}
