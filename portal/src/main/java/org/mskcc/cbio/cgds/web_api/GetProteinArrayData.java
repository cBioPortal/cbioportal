
package org.mskcc.cgds.web_api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoProteinArrayData;
import org.mskcc.cgds.dao.DaoProteinArrayInfo;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ProteinArrayData;
import org.mskcc.cgds.model.ProteinArrayInfo;

/**
 *
 * @author jj
 */
public class GetProteinArrayData {
    
    
    public static String getProteinArrayInfo(String cancerStudyStableId, ArrayList<String> targetGeneList, String type) 
            throws DaoException {
        return getProteinArrayInfo(DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId).getInternalId(),
                targetGeneList, type);
    }
    
    public static String getProteinArrayInfo(int cancerStudyId, ArrayList<String> targetGeneList, String type) 
            throws DaoException {
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        
        StringBuilder sb = new StringBuilder("ARRAY_ID\tARRAY_TYPE\tGENE\t"
                + "RESIDUE\n");
        
        ArrayList<ProteinArrayInfo> pais;
        
        Set<String> types = type==null?null:Collections.singleton(type);
        if (targetGeneList==null) {
            pais = daoPAI.getProteinArrayInfoForType(cancerStudyId,types);
        } else {
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
            Set<Long> entrezIds = new HashSet<Long>();
            for (String symbol : targetGeneList) {
                CanonicalGene gene = daoGene.getGene(symbol);
                if (gene!=null) {
                    entrezIds.add(gene.getEntrezGeneId());
                }
            }
            pais = daoPAI.getProteinArrayInfoForEntrezIds(cancerStudyId, entrezIds, types);
        }
        
        for (ProteinArrayInfo pai : pais) {
            sb.append(pai.getId()); 
            sb.append('\t').append(pai.getType()); 
            sb.append('\t').append(pai.getGene()); 
            sb.append('\t').append(pai.getResidue()); 
            //sb.append('\t').append(pai.getSource()); 
            //sb.append('\t').append(Boolean.toString(pai.isValidated()));
            sb.append('\n');
        }
        
        return sb.toString();
    }
    
    public static String getProteinArrayData(String cancerStudyStableId, List<String> arrayIds,
            ArrayList<String> targetCaseList, boolean arrayInfo) throws DaoException {
        Map<String,ProteinArrayInfo> mapArrayIdArray = new HashMap<String,ProteinArrayInfo>();
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        ArrayList<ProteinArrayInfo> pais;
        if (arrayIds==null || arrayIds.isEmpty()) {
            pais = daoPAI.getProteinArrayInfo(DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId)
                    .getInternalId());
        } else {
            pais = daoPAI.getProteinArrayInfo(arrayIds, null);
        }
        for (ProteinArrayInfo pid : pais){
            mapArrayIdArray.put(pid.getId(), pid);
        }
        Set<String> arrays = mapArrayIdArray.keySet();
        
        DaoProteinArrayData daoPAD = DaoProteinArrayData.getInstance();
        Map<String, Map<String,Double>> mapArrayCaseAbun = new HashMap<String,Map<String,Double>>();
        Set<String> caseIds = new HashSet<String>();
        for (ProteinArrayData pad : daoPAD.getProteinArrayData(arrays, targetCaseList)) {
            String arrayId = pad.getArrayId();
            String caseId = pad.getCaseId();
            caseIds.add(caseId);
            Map<String,Double> mapCaseAbun = mapArrayCaseAbun.get(arrayId);
            if (mapCaseAbun==null) {
                mapCaseAbun = new HashMap<String,Double>();
                mapArrayCaseAbun.put(arrayId, mapCaseAbun);
            }
            mapCaseAbun.put(caseId, pad.getAbundance());
        }
        
        StringBuilder sb = new StringBuilder();
        if (targetCaseList==null) {
            targetCaseList = new ArrayList<String>(caseIds);
        }
        
        sb.append("ARRAY_ID\t");
        if (arrayInfo) {
            sb.append("ARRAY_TYPE\tGENE\tRESIDUE\t");
        }
        sb.append(StringUtils.join(targetCaseList, "\t"));
        sb.append('\n');
        
        for (String arrayId : arrays) {
            Map<String,Double> mapCaseAbun = mapArrayCaseAbun.get(arrayId);
            if (mapCaseAbun==null) {
                continue;
            }
            
            sb.append(arrayId);
            if (arrayInfo) {
                ProteinArrayInfo pai = mapArrayIdArray.get(arrayId);
                sb.append("\t").append(pai.getType());
                sb.append("\t").append(pai.getGene());
                sb.append("\t").append(pai.getResidue());
            }
            
            for (String caseId : targetCaseList) {
                sb.append('\t');
                Double abundance = mapCaseAbun.get(caseId);
                if (abundance==null) {
                    sb.append("NaN");
                } else {
                    sb.append(abundance.toString()); 
                }
            }
                
            sb.append('\n');
        }
        
        return sb.toString();
    }
}
