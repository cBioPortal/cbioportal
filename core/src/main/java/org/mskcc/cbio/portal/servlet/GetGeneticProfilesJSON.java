package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneticProfile;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.GeneticProfile;

/**
 * Get the genetic profiles for a cancer study
 *
 * same input and output as the original web API
 * getGeneticProfiles
 * except return JSON instead of plain text
 *
 * @param cancer_study_id
 * @return JSON objects of genetic profiles
 */
public class GetGeneticProfilesJSON extends HttpServlet  {

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

        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
        if (cancerStudy != null) {

            int cancerStudyId = cancerStudy.getInternalId();

            JSONObject result = new JSONObject();
            ArrayList<GeneticProfile> list =
                    DaoGeneticProfile.getAllGeneticProfiles(cancerStudyId);

            if (list.size() > 0) {

                for (GeneticProfile geneticProfile : list) {

                    JSONObject tmpProfileObj = new JSONObject();

                    tmpProfileObj.put("Id", geneticProfile.getStableId());
                    tmpProfileObj.put("Name", geneticProfile.getProfileName());
                    tmpProfileObj.put("Description", geneticProfile.getProfileDescription());
                    tmpProfileObj.put("Genetic Alteration Type", geneticProfile.getGeneticAlterationType().toString());
                    tmpProfileObj.put("Cancer Study Id", geneticProfile.getStableId());
                    tmpProfileObj.put("Show in Analysis Tab", geneticProfile.showProfileInAnalysisTab());

                    result.put(geneticProfile.getStableId(), tmpProfileObj);
                }

                httpServletResponse.setContentType("application/json");
                PrintWriter out = httpServletResponse.getWriter();
                JSONValue.writeJSONString(result, out);
            } else {
                httpServletResponse.setContentType("application/text");
                PrintWriter out = httpServletResponse.getWriter();
                out.print("Error:  No genetic profiles available for: " + cancerStudyId);
                out.flush();
            }

        }
    }
}
