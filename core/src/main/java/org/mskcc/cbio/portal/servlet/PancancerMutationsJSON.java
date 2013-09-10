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
import org.mskcc.cbio.cgds.util.AccessControl;
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
     * iterate over all cancer studies, for each one grab all the genetic profiles,
     * but only grab the ones that are mutation profiles,
     * and for each mutation profile count samples by mutation keyword
     *
     * @param keywords  List of keywords as there are in the mutation sql table
     * @return data     Collection of (Map: String -> Object)
     * @throws DaoException
     */
    public Collection<Map<String, Object>> byKeywords(List<String> keywords) throws DaoException {
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
            throw new DaoException("no genetic_profile_ids found");
        }

        Collection<Map<String, Object>> data = DaoMutation.countSamplesWithKeywords(keywords, internalGeneticProfileIds);

        return data;
    }

    /**
     *
     * @param hugos
     * @return
     * @throws DaoException
     */
    public Collection<Map<String, Object>> byHugos(List<String> hugos) throws DaoException {
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
            throw new DaoException("no genetic_profile_ids found");
        }

        Collection<Map<String, Object>> data = DaoMutation.countSamplesWithGenes(hugos, internalGeneticProfileIds);

        return data;
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

        Collection<Map<String, Object>> data = null;
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");

        String cmd = request.getParameter("cmd");
        String query = request.getParameter("q");

        if (query == null || query.equals("")) {
            throw new ServletException("no q parameter provided");
        }
        JSONArray queryTerms = (JSONArray) JSONValue.parse(query);

        if (cmd.equals("byKeywords")) {
            try {
                data = byKeywords(queryTerms);
            } catch (DaoException e) {
                throw new ServletException(e);
            }
        }

        else if (cmd.equals("byHugos")) {
            try {
                data = byHugos(queryTerms);
            } catch (DaoException e) {
                throw new ServletException(e);
            }
        }

        else {
            throw new ServletException("cmd not found");
        }

        JSONArray.writeJSONString((List) data, writer);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
