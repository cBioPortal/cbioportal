package org.mskcc.portal.servlet;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoMutSig;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.MutSig;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

    // JSON servlet for fetching MutSig data.
    // If there is no MutSig data, then return an empty JSON.


public class MutSigJSON extends HttpServlet {
    private ServletXssUtil servletXssUtil;
    public static final String SELECTED_CANCER_STUDY = "selected_cancer_type";

    //
    // Initializes the servlet.
    //
    public void init() throws ServletException {
        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException(e);
        }
    }

    // Make a map out of every mutsig
    // Add that map to the mutSigJSONArray
    // Returns the empty set, {}, if qval > 0.05 (specificed by Ethan)
    public static Map MutSigtoMap(MutSig mutsig)
    {
        Map map = new HashMap();

        if (Float.parseFloat(mutsig.getqValue().replace("<","")) <= 0.05)
        {
            map.put("gene_symbol", mutsig.getCanonicalGene().getStandardSymbol());
            map.put("num_muts", mutsig.getNumMutations());
            map.put("qval", mutsig.getqValue());
        }
        return map;
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

            DaoMutSig daoMutSig = DaoMutSig.getInstance();
            ArrayList<MutSig> mutSigList = daoMutSig.getAllMutSig(cancerStudy.getInternalId());

            for (MutSig mutsig : mutSigList) {
                Map map = MutSigtoMap(mutsig);

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
