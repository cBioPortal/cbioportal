package org.mskcc.portal.servlet;

import org.mskcc.portal.util.*;
import org.mskcc.portal.model.*;
import org.mskcc.portal.remote.*;
import org.mskcc.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintLangException;
import org.mskcc.portal.r_bridge.SurvivalPlot;
import org.owasp.validator.html.PolicyException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.rmi.RemoteException;

/**
 * This Servlet Returns a JSON Representation of all Cancer Studies and all
 * Gene Sets within the Portal.
 *
 * @author Ethan Cerami.
 */
public class PortalMetaDataJSON extends HttpServlet {

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servelt Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException      IO Error.
     */
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        XDebug xdebug = new XDebug(httpServletRequest);

        //  Cancer All Cancer Studies
        ArrayList<CancerType> cancerStudiesList = GetCancerTypes.getCancerTypes(xdebug);

        //  Get all Genomic Profiles and Case Sets for each Cancer Study
        Map rootMap = new LinkedHashMap();
        Map cancerStudyMap = new LinkedHashMap();
        rootMap.put("cancer_studies", cancerStudyMap);
        for (CancerType cancerStudy:  cancerStudiesList) {
            ArrayList<CaseSet> caseSets = GetCaseSets.getCaseSets
                    (cancerStudy.getCancerTypeId(), xdebug);

            ArrayList<GeneticProfile> geneticProfiles =
                    GetGeneticProfiles.getGeneticProfiles(cancerStudy.getCancerTypeId(), xdebug);
            JSONArray jsonGenomicProfileList = new JSONArray();
            for (GeneticProfile geneticProfile:  geneticProfiles) {
                Map map = new LinkedHashMap();
                map.put("id", geneticProfile.getId());
                map.put("alteration_type", geneticProfile.getAlterationType().toString());
                map.put("show_in_analysis_tab", geneticProfile.showProfileInAnalysisTab());
                map.put("name", geneticProfile.getName());
                map.put("description", geneticProfile.getDescription());
                jsonGenomicProfileList.add(map);
            }

            JSONArray jsonCaseList = new JSONArray();
            for (CaseSet caseSet: caseSets) {
                Map map = new LinkedHashMap();
                map.put("id", caseSet.getId());
                map.put("name", caseSet.getName());
                map.put("description", caseSet.getDescription());
                jsonCaseList.add(map);
            }
            Map jsonCancerStudySubMap = new LinkedHashMap();
            jsonCancerStudySubMap.put("name", cancerStudy.getCancerName());
            jsonCancerStudySubMap.put("description", cancerStudy.getDescription());
            jsonCancerStudySubMap.put("genomic_profiles", jsonGenomicProfileList);
            jsonCancerStudySubMap.put("case_sets", jsonCaseList);
            cancerStudyMap.put(cancerStudy.getCancerTypeId(), jsonCancerStudySubMap);
        }

        //  Get all Gene Sets
        GeneSetUtil geneSetUtil = GeneSetUtil.getInstance();
        Map jsonGeneSetMap = new LinkedHashMap();
        rootMap.put("gene_sets", jsonGeneSetMap);
        ArrayList <GeneSet> geneSetList = geneSetUtil.getGeneSetList();
        for (GeneSet geneSet:  geneSetList) {
            Map geneSetMap = new LinkedHashMap();
            geneSetMap.put("name", geneSet.getName());
            geneSetMap.put("gene_list", geneSet.getGeneList());
            jsonGeneSetMap.put(geneSet.getId(), geneSetMap);
        }

        httpServletResponse.setContentType("application/json");
        String jsonText = JSONValue.toJSONString(rootMap);
        PrintWriter writer = httpServletResponse.getWriter();
        writer.write(jsonText);
        writer.flush();
        writer.close();
    }
}