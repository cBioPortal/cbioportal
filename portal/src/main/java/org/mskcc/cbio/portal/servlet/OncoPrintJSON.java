package org.mskcc.cbio.portal.servlet;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.taskdefs.Java;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.dao.GeneticAlterationUtil;
import org.mskcc.cbio.cgds.model.*;
import org.mskcc.cbio.cgds.web_api.GetProfileData;
import org.mskcc.cbio.portal.remote.GetCaseSets;
import org.mskcc.cbio.portal.remote.GetGeneticProfiles;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OncoPrintJSON extends HttpServlet {
    private ServletXssUtil servletXssUtil;
    public static final String SELECTED_CANCER_STUDY = "selected_cancer_type";
    public static final String GENE_LIST = "gene_list";
    public static final String ACTION_NAME = "Action";

    private static Log log = LogFactory.getLog(GisticJSON.class);

    /**
     * zips together samples with their respective CNV and Mutations
     * @param samples
     * @param cnv
     * @param mut
     * @return
     */
    public ArrayList<String[]> zipSamples(ArrayList<String> samples,
                                     ArrayList<String> cnv, ArrayList<String> mut) {
        
        
        int samples_l = samples.size();
        int cnv_l = cnv.size();
        int mut_l = mut.size();
        
        if (samples_l != cnv_l || 
                cnv_l != mut_l ||
                samples_l != mut_l) {
            System.err.println("cannot zip lists of different sizes");
            System.exit(1);
        }
        
        ArrayList<String[]> zip = new ArrayList<String[]>();

        for (int i = 0; i < samples_l; i++) {
            String[] sample = new String[3];
            
            sample[0] = samples.get(i);
            sample[1] = cnv.get(i);
            sample[2] = mut.get(i);

            zip.add(sample);
        }
        return zip;
    }

    /**
     * another helper function to map the results of zipSamples to something
     * for oncoprint to work on.
     *
     * @param zipSamples
     * @return
     */
    public ArrayList<Map> normalizeZipSamples(ArrayList<String[]> zipSamples) {
        ArrayList<Map> normalized = new ArrayList<Map>();

        Map sample = new HashMap();
        for (String[] s : zipSamples) {
            sample.put("sample", s[0]);
            int cnv = Integer.parseInt(s[1]);
            if (cnv = 0) {

            }
        }
    }

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

        try {
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancer_study_id);
//            System.out.println("cancer_study_id: " + cancer_study_id);

            // get list of genes
            String genes_str = servletXssUtil.getCleanInput(request.getParameter(GENE_LIST));
            genes_str = URLDecoder.decode(genes_str, "utf-8");

            // hack to get ride of HTML entity, is there some standard way to do this?
            // &quot -> \"
            genes_str = genes_str.replaceAll("&quot;","\"");

//            System.out.println("str: " + genes_str + genes_str.getClass());
//            System.out.println("obj: " + genes_obj);
//            System.out.println("array: " + genes);
            Object genes_obj = JSONValue.parseWithException(genes_str);
            
            ArrayList<String> genes = (ArrayList<String>)  genes_obj;


            ArrayList<GeneticProfile> profileList = GetGeneticProfiles.getGeneticProfiles(cancer_study_id);
//            System.out.println(profileList);
//            System.out.println("size profileList: " + profileList.size());

            ArrayList<CaseList> caseSets = GetCaseSets.getCaseSets(cancer_study_id);
//            System.out.println(caseSets);
//            System.out.println("size caseSets: " + caseSets.size());

            // getProfileData is for getting
            // ProfileData is for working with (querying)
            
            if (caseSets.size() > 1) {
                System.out.println("Why are there more than 1 case sets for cancer_type: <"
                        + cancer_study_id + ">?");
            }

            ArrayList<String> caseList = caseSets.get(0).getCaseList();

            if (genes == null) {
                System.err.println("error is type conversion");
                System.exit(1);
            }

            DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
            JSONArray array = new JSONArray();

            for (String _g : genes) {

                CanonicalGene g = daoGeneOptimized.getGene(_g);
                if (g == null) {
                    System.out.println(_g + " -> " + g);
                }

                Map map = new HashMap();

                map.put("hugoGeneSymbol", g.getHugoGeneSymbolAllCaps());
                map.put("percentAltered", "tba...");

                ArrayList<Map> alterations = new ArrayList<Map>();

                for (GeneticProfile geneticProfile : profileList) {

                    GeneticAlterationType alterationType = geneticProfile.getGeneticAlterationType();

                    if (alterationType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION)) {
                        ArrayList<String> row = GeneticAlterationUtil.getGeneticAlterationDataRow(g,
                                caseList, geneticProfile);
                    } else if (alterationType.equals(GeneticAlterationType.MUTATION_EXTENDED)) {
                        // ...
                    }
                }
                
                map.put("alterations", alterations);
                
            }

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
        } catch (ParseException e) {
            System.out.println(ParseException.class + ":" + e);
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
