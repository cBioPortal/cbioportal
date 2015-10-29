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

package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.math.MathException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.or_analysis.ORAnalysisDiscretizedDataProxy;
import org.mskcc.cbio.portal.stats.BenjaminiHochbergFDR;

/**
 * Calculate over representation scores 
 * 
 */
public class OverRepresentationAnalysisJSON extends HttpServlet  {

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
     */
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles the HTTP POST Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
     */
    protected void doPost(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse) throws ServletException, IOException {
        
        try {
            //Extract parameters
            String cancerStudyId = httpServletRequest.getParameter("cancer_study_id");
            String _alteredCaseList = httpServletRequest.getParameter("altered_case_id_list");
            String[] alteredCaseList = _alteredCaseList.split("\\s+");
            String _unalteredCaseList = httpServletRequest.getParameter("unaltered_case_id_list");
            String[] unalteredCaseList = _unalteredCaseList.split("\\s+");
            String profileId = httpServletRequest.getParameter("profile_id");
            String[] queriedGenes = httpServletRequest.getParameter("gene_list").split("\\s+");
            String geneSetOpt = httpServletRequest.getParameter("gene_set"); //cancer genes(default), all genes, custom genes

            //calculate deep deletion and amplification separately
            String copyNumType = "none";
            if (profileId.contains("_amp")) {
                copyNumType = "amp";
                profileId = profileId.substring(0, profileId.length() - 4);
            } else if (profileId.contains("_del")) {
                copyNumType = "del";
                profileId = profileId.substring(0, profileId.length() - 4);
            }

            //Get genetic profile ID (int) & Type
            GeneticProfile gp = DaoGeneticProfile.getGeneticProfileByStableId(profileId);
            int gpId = gp.getGeneticProfileId();
            String gpStableId = gp.getStableId();
            String profileType = gp.getGeneticAlterationType().toString();

            //Get cancer study internal id (int)
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
            int cancerStudyInternalId = cancerStudy.getInternalId();
            
            //Get Internal Sample Ids (int)
            List<Integer> alteredSampleIds = new ArrayList<>();
            List<Integer> unalteredSampleIds = new ArrayList<>();
            for(String alteredSampleId : alteredCaseList) {
                Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyInternalId, alteredSampleId);   
                alteredSampleIds.add(sample.getInternalId()); 
            }   
            alteredSampleIds.retainAll(DaoSampleProfile.getAllSampleIdsInProfile(gpId));
            for(String unalteredSampleId : unalteredCaseList) {
                Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyInternalId, unalteredSampleId);   
                unalteredSampleIds.add(sample.getInternalId()); 
            }   
            unalteredSampleIds.retainAll(DaoSampleProfile.getAllSampleIdsInProfile(gpId));

            //get gene IDs
            Set<Long> entrezGeneIds = new HashSet<>();
            DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
            Set<Long> profileGeneIds = new HashSet<>();
            if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) { //get only genes that has mutations -- performance concern
                Set<CanonicalGene> profileGeneSet = DaoMutation.getGenesInProfile(gpId);
                for (CanonicalGene profileGene : profileGeneSet) {
                    profileGeneIds.add(profileGene.getEntrezGeneId());
                }
            }
            if (geneSetOpt.equals("cancer_genes")) {
                Set<CanonicalGene> cancerGeneSet = daoGeneOptimized.getCbioCancerGenes();
                for (CanonicalGene cancerGene : cancerGeneSet) {
                    entrezGeneIds.add(cancerGene.getEntrezGeneId());
                }
                if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) { //overlap two gene sets for mutation profile
                    entrezGeneIds.retainAll(profileGeneIds);
                }
            } else if (geneSetOpt.equals("all_genes")) {
                if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) {
                    entrezGeneIds.addAll(profileGeneIds);
                } else {
                    ArrayList<CanonicalGene> allGeneSet = daoGeneOptimized.getAllGenes();
                    for (CanonicalGene gene: allGeneSet) {
                        entrezGeneIds.add(gene.getEntrezGeneId());
                    }
                }
            }

            //the actual calculation
            ArrayList<ObjectNode> _result = new ArrayList<>();
            ORAnalysisDiscretizedDataProxy dataProxy =
                    new ORAnalysisDiscretizedDataProxy(
                            gpId,
                            gpStableId,
                            profileType,
                            copyNumType,
                            alteredSampleIds,
                            unalteredSampleIds,
                            queriedGenes
                    );
            if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) {
                List<Integer> sampleIds = new ArrayList<>(alteredSampleIds);
                sampleIds.addAll(unalteredSampleIds);
                HashMap mutHm = DaoMutation.getSimplifiedMutations(gpId, sampleIds, entrezGeneIds);
                //Assign every sample (included non mutated ones) values -- mutated -> Mutation Type, non-mutated -> "Non"
                for (Long entrezGeneId : entrezGeneIds) {
                    _result.add(dataProxy.processMutHm(entrezGeneId, (ArrayList)sampleIds, mutHm));
                }
            } else {
                _result = DaoGeneticAlteration.getProcessedAlterationData(
                        gpId,
                        entrezGeneIds,
                        dataProxy
                );
            }


            //remove result entries without p values
            for (int i = 0; i < _result.size(); i++) {
                if (_result.get(i) == null) {
                    _result.remove(i);
                }
            }

            //sort result by p values
            Collections.sort(_result, new pValueComparator());

            //calculate adjusted p values (q values)
            double[] originalPvalues = new double[_result.size()];
            for (int i = 0; i < _result.size(); i++) {
                originalPvalues[i] = _result.get(i).get("p-Value").asDouble();
            }
            BenjaminiHochbergFDR bhFDR = new BenjaminiHochbergFDR(originalPvalues);
            bhFDR.calculate();
            double[] adjustedPvalues = bhFDR.getAdjustedPvalues();
            for (int j = 0; j < _result.size(); j++) {
                _result.get(j).put("q-Value", adjustedPvalues[j]);
            }

            //convert array to arraynode
            JsonNodeFactory factory = JsonNodeFactory.instance;
            ArrayNode result = new ArrayNode(factory);
            for (ObjectNode _result_node : _result) {
                result.add(_result_node);
            }

            //return/write back result
            ObjectMapper mapper = new ObjectMapper();
            httpServletResponse.setContentType("application/json");
            PrintWriter out = httpServletResponse.getWriter();
            mapper.writeValue(out, result);

        } catch (DaoException ex) {
            System.out.println(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        } catch (MathException ex) {
            System.out.println(ex.getMessage());
        }
    }

    class pValueComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            ObjectNode obj1 = (ObjectNode) o1;
            ObjectNode obj2 = (ObjectNode) o2;
            if (obj1.get("p-Value").asDouble() > obj2.get("p-Value").asDouble()) return 1;
            else if (obj1.get("p-Value").asDouble() == obj2.get("p-Value").asDouble()) return 0;
            else return -1;
        }
    }

}



