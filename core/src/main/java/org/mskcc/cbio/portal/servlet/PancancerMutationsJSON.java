/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
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
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.mskcc.cbio.portal.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneticProfile;
import org.mskcc.cbio.cgds.dao.DaoMutation;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.GeneticAlterationType;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Gideon Dresdner <dresdnerg@cbio.mskcc.org>
 *
 */
public class PancancerMutationsJSON extends HttpServlet {
    private ServletXssUtil servletXssUtil;
    private static Log log = LogFactory.getLog(PancancerMutationsJSON.class);

    /**
     * Initializes the servlet.
     *
     * @throws ServletException
     */
    public void init() throws ServletException {
        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException(e);
        }
    }

    /**
     * the request requires a parameter "mutation_keys" which is a JSON list of strings.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Collection<String> hugos = new ArrayList<String>();
        hugos.add("EGFR");

        Collection<Integer> profileIds = new ArrayList<Integer>();
        profileIds.add(5);

        try {
            Collection<Map<String, Object>> data = DaoMutation.countSamplesWithGenes(hugos, profileIds);

            System.out.println(data);

        } catch (DaoException e) {
            throw new ServletException(e);
        }

        String mutationKeysRawJson = request.getParameter("mutation_keys");

        if (mutationKeysRawJson == null || mutationKeysRawJson.equals("")) {
            throw new ServletException("no mutation_keys parameter provided");
        }

        JSONArray mutationKeys = (JSONArray) JSONValue.parse(mutationKeysRawJson);

        // iterate over all cancer studies, for each one grab all the genetic profiles,
        // only grab the ones that are mutation profiles,
        // and for each mutation profile count samples by mutation keyword
        List<CancerStudy> allCancerStudies = DaoCancerStudy.getAllCancerStudies();
        Collection<Integer> internalGeneticProfileIds = new ArrayList<Integer>();
        for (CancerStudy cancerStudy : allCancerStudies) {
            Integer internalId = cancerStudy.getInternalId();

            List<GeneticProfile> geneticProfiles = DaoGeneticProfile.getAllGeneticProfiles(internalId);

            for (GeneticProfile geneticProfile : geneticProfiles) {

                if (geneticProfile.getGeneticAlterationType().equals(GeneticAlterationType.MUTATION_EXTENDED)) {
                    internalGeneticProfileIds.add(geneticProfile.getGeneticProfileId());
                }
            }
        }

        if (internalGeneticProfileIds.isEmpty()) {
            throw new ServletException("no genetic_profile_ids found");
        }

        Collection<Map<String, Object>> data;
        try {
            data = DaoMutation.countSamplesWithKeywords(mutationKeys, internalGeneticProfileIds);
        } catch (DaoException e) {
            throw new ServletException(e);
        }


        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");

        JSONArray.writeJSONString((List) data, writer);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
