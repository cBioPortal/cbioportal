/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.web_api;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.rmi.RemoteException;

/**
 *
 * @author jj
 */
public class GetProteinArrayData {
    
    public static Set<String> getProteinArrayTypes() throws DaoException {
        return DaoProteinArrayInfo.getInstance().getAllAntibodyTypes();
    }
    
    
    public static Map<String,ProteinArrayInfo> getProteinArrayInfo(
            String cancerStudyStableId, List<String> geneList, Collection<String> types) throws DaoException {
        return getProteinArrayInfo(DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId).getInternalId(),
                geneList, types);
    }

    /**
     * 
     * @param geneList
     * @param type
     * @param xdebug
     * @return key: array id; value: array info
     * @throws DaoException 
     */
    public static Map<String,ProteinArrayInfo> getProteinArrayInfo(
            int cancerStudyId, List<String> geneList, Collection<String> types) throws DaoException {
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        List<ProteinArrayInfo> pais;
        
        if (geneList==null) {
            pais = daoPAI.getProteinArrayInfoForType(cancerStudyId, types);
        } else {
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
            Set<Long> entrezIds = new HashSet();
            for (String gene : geneList) {
                CanonicalGene cGene = daoGene.getGene(gene);
                if (cGene!=null) {
                    entrezIds.add(cGene.getEntrezGeneId());
                }
            }
            pais = daoPAI.getProteinArrayInfoForEntrezIds(cancerStudyId, entrezIds, types);
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
     * @param sampleIds
     * @param xdebug
     * @return Map &lt; arrayId, Map &lt; caseId,Abundance &gt; &gt;
     * @throws RemoteException 
     */
    public static Map<String,Map<String,Double>> getProteinArrayData(int cancerStudyId, 
            Collection<String> proteinArrayIds, Collection<String> sampleIds)
            throws RemoteException, DaoException {

        List<Integer> internalSampleIds = InternalIdUtil.getInternalSampleIds(cancerStudyId, new ArrayList<String>(sampleIds));

        List<ProteinArrayData> pads = DaoProteinArrayData.getInstance()
                .getProteinArrayData(cancerStudyId, proteinArrayIds, internalSampleIds);
        
        Map<String,Map<String,Double>> ret = new HashMap<String,Map<String,Double>>();
        
        for (ProteinArrayData pad : pads) {
            String arrayId = pad.getArrayId();
            Sample sample = DaoSample.getSampleById(pad.getSampleId());
            double abun = pad.getAbundance();
            if (Double.isNaN(abun))
                continue;
            
            Map<String,Double> mapCaseAbun = ret.get(arrayId);
            if (mapCaseAbun==null) {
                mapCaseAbun = new HashMap<String,Double>();
                ret.put(arrayId, mapCaseAbun);
            }
            mapCaseAbun.put(sample.getStableId(), abun);
        }
            
        return ret;
    }
    
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
            ArrayList<String> targetSampleList, boolean arrayInfo) throws DaoException {
        Map<String,ProteinArrayInfo> mapArrayIdArray = new HashMap<String,ProteinArrayInfo>();
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        ArrayList<ProteinArrayInfo> pais;
        int studyId = DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId).getInternalId();
        if (arrayIds==null || arrayIds.isEmpty()) {
            pais = daoPAI.getProteinArrayInfo(studyId);
        } else {
            pais = daoPAI.getProteinArrayInfo(arrayIds, null);
        }
        for (ProteinArrayInfo pid : pais){
            mapArrayIdArray.put(pid.getId(), pid);
        }
        Set<String> arrays = mapArrayIdArray.keySet();
        
        DaoProteinArrayData daoPAD = DaoProteinArrayData.getInstance();
        Map<String, Map<String,Double>> mapArrayCaseAbun = new HashMap<String,Map<String,Double>>();
        List<Integer> internalSampleIds = InternalIdUtil.getInternalSampleIds(studyId, targetSampleList);
        Set<String> sampleIds = new HashSet<String>();
        for (ProteinArrayData pad : daoPAD.getProteinArrayData(studyId, arrays, internalSampleIds)) {
            String arrayId = pad.getArrayId();
            Sample sample = DaoSample.getSampleById(pad.getSampleId());
            sampleIds.add(sample.getStableId());
            Map<String,Double> mapCaseAbun = mapArrayCaseAbun.get(arrayId);
            if (mapCaseAbun==null) {
                mapCaseAbun = new HashMap<String,Double>();
                mapArrayCaseAbun.put(arrayId, mapCaseAbun);
            }
            mapCaseAbun.put(sample.getStableId(), pad.getAbundance());
        }
        
        StringBuilder sb = new StringBuilder();
        if (targetSampleList==null) {
            targetSampleList = new ArrayList<String>(sampleIds);
        }
        
        sb.append("ARRAY_ID\t");
        if (arrayInfo) {
            sb.append("ARRAY_TYPE\tGENE\tRESIDUE\t");
        }
        sb.append(StringUtils.join(targetSampleList, "\t"));
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
            
            for (String sampleId : targetSampleList) {
                sb.append('\t');
                Double abundance = mapCaseAbun.get(sampleId);
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
