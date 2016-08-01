/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoMutSig;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.MutSig;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * JSON servlet for fetching MutSig data.
 * If there is no MutSig data, then return an empty JSON.
 * @author Gideon Dresdner
 */
public class MutSigJSON extends HttpServlet {
    public static final String SELECTED_CANCER_STUDY = "selected_cancer_type";
    private static Log log = LogFactory.getLog(MutSigJSON.class);

    //
    // Initializes the servlet.
    //
    public void init() throws ServletException {
        super.init();
    }

    // Make a map out of every mutsig
    // Add that map to the mutSigJSONArray
    // Returns the empty set, {}, if qval > 0.01 (specificed by Ethan)
    public static Map MutSigtoMap(MutSig mutsig) {
        Map map = new HashMap();

        map.put("gene_symbol", mutsig.getCanonicalGene().getStandardSymbol());
        map.put("num_muts", mutsig.getNumMutations());
        map.put("qval", mutsig.getqValue());

        return map;
    }

    /**
     * Sort Mutsigs by rank, which is determined by q-value.
     * So actually we are sorting by q-value
     */
    private class sortMutsigByRank implements Comparator<MutSig> {
        public int compare(MutSig mutSig1, MutSig mutSig2) {

            // Collections.sort is in ascending order and
            // we want the smallest q-value at the top
            return mutSig1.getRank() - mutSig2.getRank();
        }
    }

    //
    // Handles HTTP GET request
    //
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {

        String cancer_study_id = request.getParameter(SELECTED_CANCER_STUDY);
        JSONArray mutSigJSONArray = new JSONArray();

        try {
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancer_study_id);

            if (log.isDebugEnabled()) {
                log.debug("cancerStudyId passed to MutSigJSON: " + cancerStudy.getInternalId());
            }

            List<MutSig> mutSigList = DaoMutSig.getAllMutSig(cancerStudy.getInternalId());

            if (log.isDebugEnabled()) {
                log.debug("no of mutsigs associated with cancerStudy: " + mutSigList.size() + "\n");
            }

            Collections.sort(mutSigList, new sortMutsigByRank());

            int i = 0;
            for (MutSig mutsig : mutSigList) {

//                log.debug("" + mutsig.toString() + " " + i++);

                Map map = MutSigtoMap(mutsig);

//                log.debug(map);

                if (!map.isEmpty()) {
                    mutSigJSONArray.add(map);
                }
            }
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();

            try {
                JSONValue.writeJSONString(mutSigJSONArray, out);
            } finally {
                out.close();
            }

        } catch (DaoException e) {
            throw new ServletException(e);
        }
    }

    // Just in case the request changes from GET to POST
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        doGet(request, response);
    }

}