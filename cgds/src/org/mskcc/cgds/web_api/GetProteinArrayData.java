
package org.mskcc.cgds.web_api;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoProteinArrayData;
import org.mskcc.cgds.dao.DaoProteinArrayInfo;
import org.mskcc.cgds.dao.DaoProteinArrayTarget;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ProteinArrayData;
import org.mskcc.cgds.model.ProteinArrayInfo;
import org.mskcc.cgds.model.ProteinArrayTarget;

/**
 *
 * @author jj
 */
public class GetProteinArrayData {
    
    public static String getProteinArrayInfo(ArrayList<String> targetGeneList, String type) 
            throws DaoException {
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        DaoProteinArrayTarget daoPAT = DaoProteinArrayTarget.getInstance();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        
        StringBuilder sb = new StringBuilder("GENE_ID\tCOMMON\tARRAY_ID\tARRAY_TYPE\t"
                + "RESIDUE\tANTIBODY_SOURCE\tVALIDATED\n");
        
        for (String geneSymbol:  targetGeneList) {
            CanonicalGene canonicalGene = daoGeneOptimized.getGene(geneSymbol);
            if (canonicalGene == null) continue;
            
            long entrez = canonicalGene.getEntrezGeneId();
            
            for (ProteinArrayTarget pat : daoPAT.getProteinArrayTarget(entrez)) {
                String arrayid = pat.getArrayId();
                String residue = pat.getResidue();
                ProteinArrayInfo pai = daoPAI.getProteinArrayInfo(arrayid);
                if (type!=null && !type.isEmpty() && !type.equals(pai.getType()))
                    continue;
                
                
                sb.append(entrez); sb.append('\t');
                sb.append(geneSymbol); sb.append('\t');
                sb.append(arrayid); sb.append('\t');
                sb.append(pai.getType()); sb.append('\t');
                sb.append(residue); sb.append('\t');
                sb.append(pai.getSource()); sb.append('\t');
                sb.append(Boolean.toString(pai.isValidated()));
                sb.append('\n');
            }
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
