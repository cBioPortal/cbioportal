package org.mskcc.cbio.portal.servlet;

import java.io.*;
import org.json.simple.JSONObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * same input and output as the original web API
 * getGeneticProfiles
 * except return JSON instead of plain text
 */
public class GetGeneticProfilesJSON extends HttpServlet {

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

        JSONObject profile1 = new JSONObject();
        profile1.put("name", "Mutations");
        profile1.put("id", "ov_tcga_mutations");

        JSONObject profile2 = new JSONObject();
        profile1.put("name", "RPPA");
        profile1.put("id", "ov_tcga_rppa");


        JSONObject jsonObj = new JSONObject();
        jsonObj.put("profile1", profile1);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(jsonObj);
        out.flush();

    }
}
