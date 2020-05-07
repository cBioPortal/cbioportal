/*
 * Copyright (c) 2012 - 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.mskcc.cbio.portal.repository.MutationRepositoryLegacy;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.model.converter.MutationModelConverter;
import org.mskcc.cbio.portal.stats.BenjaminiHochbergFDR;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.util.EnrichmentsAnalysisUtil;
import org.mskcc.cbio.portal.util.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * Calculate over representation scores
 *
 */
public class EnrichmentsJSON extends HttpServlet  {


    private final int bin = 3000; //size of genes for each thread
    private final JsonNodeFactory factory = JsonNodeFactory.instance;
    private final ArrayNode result = new ArrayNode(factory);
    
    // class which process access control to cancer studies
    private AccessControl accessControl;
    
    /**
     * Initializes the servlet.
     */
    public void init() throws ServletException {
        super.init();
        accessControl = SpringUtil.getAccessControl();
    }

    @Autowired
    private MutationRepositoryLegacy mutationRepositoryLegacy;

    @Autowired
    private MutationModelConverter mutationModelConverter;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
                config.getServletContext());
        accessControl = SpringUtil.getAccessControl();
    }

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
        	CancerStudy cancerStudy = null;
            //Extract parameters
            String cancerStudyId = httpServletRequest.getParameter("cancer_study_id");
			if (cancerStudyId != null) {
				cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
				if (cancerStudy == null
						|| accessControl.isAccessibleCancerStudy(cancerStudy.getCancerStudyStableId()).size() == 0) {
					return;
				}
			} else {
				return;
			}
            String _alteredCaseList = httpServletRequest.getParameter("altered_case_id_list");
            String[] alteredCaseList = _alteredCaseList.split("\\s+");
            String _unalteredCaseList = httpServletRequest.getParameter("unaltered_case_id_list");
            String[] unalteredCaseList = _unalteredCaseList.split("\\s+");
            String profileId = httpServletRequest.getParameter("profile_id");
            String[] queriedGenes = httpServletRequest.getParameter("gene_list").split("\\s+");

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
            final int gpId = gp.getGeneticProfileId();
            String gpStableId = gp.getStableId();
            GeneticAlterationType profileType = gp.getGeneticAlterationType();
            
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
            if (profileType == GeneticAlterationType.MUTATION_EXTENDED) { //get only genes that has mutations -- performance concern
                Set<CanonicalGene> profileGeneSet = DaoMutation.getGenesInProfile(gpId);
                for (CanonicalGene profileGene : profileGeneSet) {
                    profileGeneIds.add(profileGene.getEntrezGeneId());
                }
            }
            if (profileType == GeneticAlterationType.MUTATION_EXTENDED) {
                entrezGeneIds.addAll(profileGeneIds);
            } else {
                ArrayList<CanonicalGene> allGeneSet = daoGeneOptimized.getAllGenes();
                for (CanonicalGene gene: allGeneSet) {
                    entrezGeneIds.add(gene.getEntrezGeneId());
                }
            }

            //the actual calculation
            final List synced_result = Collections.synchronizedList(new ArrayList());
            synced_result.clear();
            final EnrichmentsAnalysisUtil dataProxy =
                    new EnrichmentsAnalysisUtil(
                            gpStableId,
                            profileType,
                            copyNumType,
                            alteredSampleIds,
                            unalteredSampleIds,
                            queriedGenes
                    );
            if (profileType == GeneticAlterationType.MUTATION_EXTENDED) {
                final List<Integer> sampleIds = new ArrayList<>(alteredSampleIds);
                sampleIds.addAll(unalteredSampleIds);
                List<Integer> intEntrezGeneIds = new ArrayList<>(entrezGeneIds.size());
                for (Long entrezGeneId : entrezGeneIds) {
                    intEntrezGeneIds.add(entrezGeneId.intValue());
                }
                final Map mutHm = mutationModelConverter.convertSampleIdAndEntrezGeneIdToMap(
                        mutationRepositoryLegacy.getSimplifiedMutations(sampleIds, intEntrezGeneIds, gpId));


                //multi-threading settings
                int nThread = (int)Math.floor(entrezGeneIds.size() / bin) + 1;
                Thread[] threads = new Thread[nThread];
                final List<Set<Long>> gene_short_lists = splitGenes(bin, nThread, entrezGeneIds);
                final AtomicInteger result_index = new AtomicInteger(-1);

                for (int i = 0; i < nThread; i++) {
                    threads[i] = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            result_index.incrementAndGet();
                            Set<Long> gene_short_list = gene_short_lists.get(result_index.get());
                            for (Long entrezGeneId : gene_short_list) {
                                try {
                                    synced_result.add(dataProxy.processMutHm(entrezGeneId, (ArrayList)sampleIds, mutHm));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    threads[i].start();
                }
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    }catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }

            } else {

                int genes_length = DaoGeneticAlteration.getGenesCountInProfile(gpId);
                int nThread = (int)Math.floor(genes_length / bin) + 1;
                Thread[] threads = new Thread[nThread];
                final AtomicInteger result_index = new AtomicInteger(-1);

                for (int i = 0; i < nThread; i++) {
                    threads[i] = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            result_index.incrementAndGet();
                            try {
                                int offSet = result_index.get() * bin;
                                synced_result.addAll(DaoGeneticAlteration.getProcessedAlterationData(
                                        gpId,
                                        offSet,
                                        dataProxy
                                ));
                            } catch (DaoException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    threads[i].start();
                }
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    }catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

            ArrayList<ObjectNode> _result = new ArrayList<>();
            synchronized(synced_result) {
                Iterator i = synced_result.iterator();
                while (i.hasNext()) {
                    ObjectNode obn = (ObjectNode)i.next();
                    if (obn != null) {
                        _result.add(obn);
                    }
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

            result.removeAll();
            for (ObjectNode _result_node : _result) {
                result.add(_result_node);
            }
            
            //return/write back result
            ObjectMapper mapper = new ObjectMapper();
            httpServletResponse.setContentType("application/json");
            PrintWriter out = httpServletResponse.getWriter();
            mapper.writeValue(out, result);

        }catch (Exception ex) {
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

    private List<Set<Long>> splitGenes(int bin, int nThread, Set<Long> entrezGeneIds) {
        List<Long> all_genes = new ArrayList<>(entrezGeneIds);
        List<Set<Long>> gene_short_lists = new ArrayList<>();
        for (int i = 0; i < nThread; i++) {
            List<Long> gene_short_list;
            if ((i + 1) * bin >= all_genes.size()) {
                gene_short_list = new ArrayList<>(all_genes.subList(i * bin, all_genes.size() - 1));
                gene_short_list.add(all_genes.get(all_genes.size() - 1));
            } else {
                gene_short_list = new ArrayList<>(all_genes.subList(i * bin, (i + 1) * bin));
            }
            Set<Long> gene_short_set = new HashSet<>(gene_short_list);
            gene_short_lists.add(gene_short_set);
        }
        return gene_short_lists;
    }

}



