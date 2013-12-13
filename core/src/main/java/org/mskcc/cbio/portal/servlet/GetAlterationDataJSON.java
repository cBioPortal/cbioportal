package org.mskcc.cbio.portal.servlet;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.util.CoExpUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

/**
 * Author: yichao
 * Date: Dec 2013
 *
 * @param : Gene Symbol
 * @param : Cancer Study Id
 * @param : Case Set Id / Case Ids Key
 * @return : Array of JSON object [case id, alteration value]
 */

public class GetAlterationDataJSON extends HttpServlet {

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws javax.servlet.ServletException
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
        String[] geneIdList = httpServletRequest.getParameter("gene_list").split("\\s+");
        String caseSetId = httpServletRequest.getParameter("case_set_id");
        String caseIdsKey = httpServletRequest.getParameter("case_ids_key");

        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

        try {
            Collection<Long> queriedGeneIds = new ArrayList<>();
            for (String geneId: geneIdList) {
                CanonicalGene geneObj = daoGeneOptimized.getGene(geneId);
                queriedGeneIds.add(geneObj.getEntrezGeneId());
            }
            GeneticProfile final_gp = CoExpUtil.getPreferedGeneticProfile(cancerStudyIdentifier);
            JSONObject result = getExpressionMap(queriedGeneIds, final_gp.getGeneticProfileId(), caseSetId, caseIdsKey);

            httpServletResponse.setContentType("application/json");
            PrintWriter out = httpServletResponse.getWriter();
            JSONValue.writeJSONString(result, out);

        } catch (DaoException e) {
            System.out.println(e.getMessage());
        }
    }

    private JSONObject getExpressionMap(Collection<Long> genes, int profileId, String caseSetId, String caseIdsKey) throws DaoException {

        ArrayList<String> caseIds = CoExpUtil.getCaseIds(caseSetId, caseIdsKey);

        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        Map<Long, HashMap<String, String>> mapStr = daoGeneticAlteration.getGeneticAlterationMap(profileId, genes);

        JSONObject _result = new JSONObject();
        for (Map.Entry<Long, HashMap<String, String>> entry : mapStr.entrySet()) {
            Long _gene = entry.getKey();
            Map<String, String> mapCaseValueStr = entry.getValue();
            ArrayList<JSONObject> _geneArr = new ArrayList<>();
            for (int i = 0; i < caseIds.size(); i++) {
                String caseId = caseIds.get(i);
                String value = mapCaseValueStr.get(caseId);
                JSONObject _datum = new JSONObject();
                _datum.put("caseId", caseId);
                _datum.put("value", value);
                _geneArr.add(_datum);
            }
            DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
            CanonicalGene _geneObj = daoGeneOptimized.getGene(_gene);
            _result.put(_geneObj.getHugoGeneSymbolAllCaps(), _geneArr);
        }
        return _result;
    }
}
