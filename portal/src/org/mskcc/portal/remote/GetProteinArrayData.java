package org.mskcc.portal.remote;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoProteinArrayData;
import org.mskcc.cgds.dao.DaoProteinArrayInfo;
import org.mskcc.cgds.model.ProteinArrayData;
import org.mskcc.cgds.model.ProteinArrayInfo;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.portal.util.XDebug;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GetProteinArrayData {
    
    public static Set<String> getProteinArrayTypes() throws DaoException {
        return DaoProteinArrayInfo.getInstance().getAllAntibodyTypes();
    }

    /**
     * 
     * @param geneList
     * @param type
     * @param xdebug
     * @return key: array id; value: array info
     * @throws DaoException 
     */
    public static Map<String,ProteinArrayInfo> getProteinArrayInfo(List<String> geneList,
                                      Collection<String> types, XDebug xdebug) throws DaoException {
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        List<ProteinArrayInfo> pais;
        
        if (geneList==null) {
            pais = daoPAI.getProteinArrayInfoForType(types);
        } else {
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
            Set<Long> entrezIds = new HashSet();
            for (String gene : geneList) {
                CanonicalGene cGene = daoGene.getGene(gene);
                if (cGene!=null) {
                    entrezIds.add(cGene.getEntrezGeneId());
                }
            }
            pais = daoPAI.getProteinArrayInfoForEntrezIds(entrezIds, types);
        }
        
        Map<String,ProteinArrayInfo> ret = new HashMap<String,ProteinArrayInfo>();
        for (ProteinArrayInfo pai : pais) {
            ret.put(pai.getId(), pai);
        }
        
        return ret;
    }
    
    /**
     * 
     * @param proteinArrayIds
     * @param caseIds
     * @param xdebug
     * @return Map &lt; arrayId, Map &lt; caseId,Abundance &gt; &gt;
     * @throws RemoteException 
     */
    public static Map<String,Map<String,Double>> getProteinArrayData(Collection<String> proteinArrayIds,
            Collection<String> caseIds, XDebug xdebug) throws RemoteException, DaoException {
        List<ProteinArrayData> pads = DaoProteinArrayData.getInstance().getProteinArrayData(proteinArrayIds, caseIds);
        
        Map<String,Map<String,Double>> ret = new HashMap<String,Map<String,Double>>();
        
        for (ProteinArrayData pad : pads) {
            String arrayId = pad.getArrayId();
            String caseId = pad.getCaseId();
            double abun = pad.getAbundance();
            if (Double.isNaN(abun))
                continue;
            
            Map<String,Double> mapCaseAbun = ret.get(arrayId);
            if (mapCaseAbun==null) {
                mapCaseAbun = new HashMap<String,Double>();
                ret.put(arrayId, mapCaseAbun);
            }
            mapCaseAbun.put(caseId, abun);
        }
            
        return ret;
    }
}
