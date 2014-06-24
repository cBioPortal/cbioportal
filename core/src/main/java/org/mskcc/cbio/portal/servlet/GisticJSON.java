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

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGistic;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Gistic;
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
 * JSON servlet for fetching Gistic data.
 * If there is no Gistic data, then return an empty JSON.
 * @author Gideon Dresdner
 */
public class GisticJSON extends HttpServlet {
    public static final String SELECTED_CANCER_STUDY = "selected_cancer_type";
    private static Log log = LogFactory.getLog(GisticJSON.class);

    /**
     * Initializes the servlet.
     *
     * @throws ServletException
     */
    public void init() throws ServletException {
        super.init();
    }

    /**
     * Make a map out of every mutsig
     * Add that map to the mutSigJSONArray
     * Returns the empty set, {}, if qval > 0.01 (specificed by Ethan)
     */
    public static Map Gistic_toMap(Gistic gistic) {
        Map map = new HashMap();

        map.put("chromosome", gistic.getChromosome());
        map.put("cytoband", gistic.getCytoband());
        map.put("peakStart", gistic.getPeakStart());
        map.put("peakEnd", gistic.getPeakEnd());

        // convert CanonicalGenes in ROI to strings
        ArrayList<String> nonSangerGenes = new ArrayList<String>();
        ArrayList<String> sangerGenes = new ArrayList<String>();

        for (CanonicalGene g : gistic.getGenes_in_ROI()) {
            try {
                if (g.isSangerGene()) {
                    sangerGenes.add(g.getHugoGeneSymbolAllCaps());
                } else {
                    nonSangerGenes.add(g.getHugoGeneSymbolAllCaps());
                }
            } catch (DaoException e) {
                // assume that it is not a Sanger Gene if causes an exception
                nonSangerGenes.add(g.getHugoGeneSymbolAllCaps());

                if (log.isDebugEnabled()) {
                    log.debug(e + " :gene <" + g +">");
                }
            }
        }

        map.put("sangerGenes", sangerGenes);
        map.put("nonSangerGenes", nonSangerGenes);
        map.put("qval", gistic.getqValue());
        map.put("ampdel", gistic.getAmp());
        
        return map;
    }

    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String cancer_study_id = request.getParameter(SELECTED_CANCER_STUDY);

        try {
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancer_study_id);
            
            if (log.isDebugEnabled()) {
                log.debug("cancerStudyId passed to GisticJSON: " + cancerStudy.getInternalId()) ;
            }

            ArrayList<Gistic> gistics = DaoGistic.getAllGisticByCancerStudyId(cancerStudy.getInternalId());

            if (log.isDebugEnabled()) {
                log.debug("list of gistics associated with cancerStudy: " + gistics) ;
            }

//            Collections.sort(gistics, new sortMutsigByRank());

            JSONArray gisticJSONArray = new JSONArray();

            for (Gistic gistic : gistics) {
                Map map = Gistic_toMap(gistic);

                if (!map.isEmpty()) {
                    gisticJSONArray.add(map);
                }
            }
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();

            try {
                JSONValue.writeJSONString(gisticJSONArray, out);
            } finally {
                out.close();
            }
        } catch (DaoException e) {
            log.debug(e);
            throw new ServletException(e);
        }
    }

    /**
     * Just in case the request changes from GET to POST
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        doGet(request, response);
    }
}