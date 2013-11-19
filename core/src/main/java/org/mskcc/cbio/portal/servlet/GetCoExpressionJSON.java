/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/

package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.json.simple.JSONObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

/**
 * Get the top co-expressed genes for queried genes
 *
 * @param : cancer study id
 * @param : queried genes
 * @return : JSON objects of co-expression under the same cancer_study
 * (but always the mrna genetic profile)
 */
public class GetCoExpressionJSON extends HttpServlet  {

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

        String cancerStudyIdentifier = httpServletRequest.getParameter("cancer_study_id");
        String geneListStr = httpServletRequest.getParameter("gene_list");

        System.out.println("param : cancer study id: " + cancerStudyIdentifier);
        System.out.println("param : geneListStr: " + geneListStr);

        //Find the qualified genetic profile under the queried cancer study
        CancerStudy cs = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
        ArrayList<GeneticProfile> gps = DaoGeneticProfile.getAllGeneticProfiles(cs.getInternalId());
        GeneticProfile final_gp = null;
        for (GeneticProfile gp : gps) {
            // TODO: support miRNA later
            if (gp.getGeneticAlterationType() == GeneticAlterationType.MRNA_EXPRESSION) {
                //rna seq profile (no z-scores applied) holds the highest priority)
                if (gp.getStableId().toLowerCase().contains("rna_seq") &&
                        !gp.getStableId().toLowerCase().contains("zscores")) {
                    final_gp = gp;
                    break;
                } else if (!gp.getStableId().toLowerCase().contains("zscores")) {
                    final_gp = gp;
                }
            }
        }

        System.out.println("Genetic Profile Name: " + final_gp.getProfileName());

        //Convert gene symbol (string) to gene ID (int)
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        String[] geneList = geneListStr.split("\\s+");
        Collection<Long> queryGenes = new ArrayList<Long>();
        for (String gene_item : geneList) {
            CanonicalGene geneObj = daoGeneOptimized.getGene(gene_item);
            queryGenes.add(geneObj.getEntrezGeneId());
        }

        System.out.println("Queried Genes Id: ");
        for (Long gene: queryGenes) {
            System.out.print(gene + ", ");
        }
        System.out.print("\n");

        try {
            Map<Long,double[]> map = getExpressionMap(final_gp.getGeneticProfileId(), queryGenes);
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                double[] _scoreSets = (double[])pairs.getValue();
                for (double _score : _scoreSets) {
                    System.out.print(pairs.getKey() + " --> " + _score + ", ");
                }
                it.remove();
            }
        } catch (DaoException e) {
            System.out.println(e.getMessage());
        }

//        ArrayList<JSONObject> result = new ArrayList<JSONObject>();
//        if (final_gp != null) {
//            try {
//                ArrayList<Coexpression> coExpList = DaoCoexpression.getCoexpression(queryGenes, final_gp.getGeneticProfileId());
//                for (Coexpression i : coExpList) {
//                    JSONObject tmpProfileObj = new JSONObject();
//                    tmpProfileObj.put("GENE_1", i.getGene1());
//                    tmpProfileObj.put("GENE_2", i.getGene2());
//                    tmpProfileObj.put("GENETIC_PROFILE_ID", i.getProfileId());
//                    tmpProfileObj.put("PEARSON", i.getPearson());
//                    tmpProfileObj.put("SPEARMAN", i.getSpearman());
//                    result.add(tmpProfileObj);
//                }
//                httpServletResponse.setContentType("application/json");
//                PrintWriter out = httpServletResponse.getWriter();
//                JSONValue.writeJSONString(result, out);
//            } catch (DaoException e) {
//                System.out.println("Dao Exception: Getting Co-expression List");
//            }
//        } else {
//            httpServletResponse.setContentType("application/text");
//            PrintWriter out = httpServletResponse.getWriter();
//            out.print("Error:  No genetic profiles available for: " + cancerStudyIdentifier);
//            out.flush();
//        }
    }

    private Map<Long,double[]> getExpressionMap(int profileId, Collection<Long> queryGenes) throws DaoException {
        ArrayList<String> orderedCaseList = DaoGeneticProfileCases.getOrderedCaseList(profileId);
        int nCases = orderedCaseList.size();
        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        Map<Long, HashMap<String, String>> mapStr = daoGeneticAlteration.getGeneticAlterationMap(profileId, queryGenes);
        Map<Long, double[]> map = new HashMap<Long, double[]>(mapStr.size());
        for (Map.Entry<Long, HashMap<String, String>> entry : mapStr.entrySet()) {
            Long gene = entry.getKey();
            Map<String, String> mapCaseValueStr = entry.getValue();
            double[] values = new double[nCases];
            for (int i = 0; i < nCases; i++) {
                String caseId = orderedCaseList.get(i);
                String value = mapCaseValueStr.get(caseId);
                Double d;
                try {
                    d = Double.valueOf(value);
                } catch (Exception e) {
                    d = Double.NaN;
                }
                if (d!=null && !d.isNaN()) {
                    values[i]=d;
                }
            }
            map.put(gene, values);
        }
        return map;
    }

}