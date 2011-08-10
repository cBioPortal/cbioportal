
package org.mskcc.cgds.web_api;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

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
    
    public static String getProteinArrayInfo(ArrayList<String> targetGeneList, String type) 
            throws DaoException {
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        
        StringBuilder sb = new StringBuilder("GENE\tARRAY_ID\tARRAY_TYPE\t"
                + "RESIDUE\tANTIBODY_SOURCE\tVALIDATED\n");
        
        ArrayList<ProteinArrayInfo> pais;
        
        if (targetGeneList==null) {
            pais = daoPAI.getProteinArrayInfoForType(type);
        } else {
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
            Set<Long> entrezIds = new HashSet<Long>();
            for (String symbol : targetGeneList) {
                CanonicalGene gene = daoGene.getGene(symbol);
                if (gene!=null)
                    entrezIds.add(gene.getEntrezGeneId());
            }
            pais = daoPAI.getProteinArrayInfoForEntrezIds(entrezIds, type);
        }
        
        for (ProteinArrayInfo pai : pais) {
            sb.append(pai.getGene()); sb.append('\t');
            sb.append(pai.getId()); sb.append('\t');
            sb.append(pai.getType()); sb.append('\t');
            sb.append(pai.getResidue()); sb.append('\t');
            sb.append(pai.getSource()); sb.append('\t');
            sb.append(Boolean.toString(pai.isValidated()));
            sb.append('\n');
        }
        
        return sb.toString();
    }
    
    public static String getProteinArrayData(String arrayId, ArrayList<String> targetCaseList) 
            throws DaoException {
        DaoProteinArrayData daoPAD = DaoProteinArrayData.getInstance();
        Map<String, Double> map = new HashMap<String,Double>();
        for (ProteinArrayData pad : daoPAD.getProteinArrayData(arrayId, targetCaseList)) {
            map.put(pad.getCaseId(), pad.getAbundance());
        }
        
        StringBuilder sb = new StringBuilder("CASE_ID\tABUNDANCE\n");
        if (targetCaseList==null)
            targetCaseList = new ArrayList<String>(map.keySet());
        
        for (String caseId : targetCaseList) {
            sb.append(caseId); sb.append('\t');
            Double abundance = map.get(caseId);
            if (abundance==null)
                sb.append("NaN");
            else
                sb.append(abundance.toString()); 
            sb.append('\t');
            sb.append('\n');
        }
        
        return sb.toString();
    }
}
