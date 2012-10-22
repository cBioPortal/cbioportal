package org.mskcc.cbio.portal.servlet;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.dao.GeneticAlterationUtil;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.CaseList;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.portal.remote.GetCaseSets;
import org.mskcc.cbio.portal.remote.GetGeneticProfiles;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OncoPrintJSON extends HttpServlet {
    private ServletXssUtil servletXssUtil;
    public static final String SELECTED_CANCER_STUDY = "selected_cancer_type";
    public static final String GENE_LIST = "gene_list";
    public static final String ACTION_NAME = "Action";

    private static Log log = LogFactory.getLog(GisticJSON.class);

    /*
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
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String cancer_study_id = servletXssUtil
                .getCleanInput(request.getParameter(SELECTED_CANCER_STUDY));


        String ex2 = "ABCD";
        System.out.println("ABCD: " + JSONValue.parse(ex2));

        String ex = "[TP53, MDM2, MDM4]";
        System.out.println("gene list ex: " + JSONValue.parse(ex));

        try {
            Object genes_obj = JSONValue.parseWithException(servletXssUtil
                    .getCleanInput(request.getParameter(GENE_LIST)));
            System.out.println(genes_obj);

            JSONArray genes = (JSONArray) genes_obj;
            System.out.println(genes);
        } catch (Exception e) {
            System.out.println(e);
        }
        
//
//        System.out.println(servletXssUtil.getCleanInput(request.getParameter(GENE_LIST)).getClass());
//        
//        System.out.println("ex: " + JSONValue.parse(ex));

        try {
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancer_study_id);
            
            System.out.println("cancer_study_id: " + cancer_study_id);
            
//            JSONArray array = new JSONArray();

            ArrayList<GeneticProfile> profileList = GetGeneticProfiles.getGeneticProfiles(cancer_study_id);
            ArrayList<CaseList> caseSets = GetCaseSets.getCaseSets(cancer_study_id);

            DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

//                Map map =

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();

            try {
//                JSONValue.writeJSONString(, out);
                JSONArray.writeJSONString(profileList, out);
            } finally {
                out.close();
            }
        } catch (DaoException e) {
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
        String geneList = servletXssUtil.getCleanInput(request, GENE_LIST);
        String action = servletXssUtil.getCleanInput (request, ACTION_NAME);

        System.out.println(geneList);
        System.out.println(action);

        doGet(request, response);
    }
}
