package org.mskcc.cbio.portal.servlet;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mskcc.cbio.cgds.dao.*;
import org.mskcc.cbio.cgds.model.*;
import org.mskcc.cbio.cgds.web_api.GetProfileData;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.cbio.portal.util.*;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class GeneDataJSON extends HttpServlet {
    private ServletXssUtil servletXssUtil;
    public static final String SELECTED_CANCER_STUDY = "selected_cancer_type";
    public static final String GENE_LIST = "gene_list";
    public static final String ACTION_NAME = "Action";
    // todo: can these strings be referenced directly from QueryBuilder itself?

    public static final String HUGO_GENE_SYMBOL = "hugoGeneSymbol";
    public static final String SAMPLE = "sample";
    public static final String UNALTERED_SAMPLE = "unaltered_sample";
    public static final String ALTERATION = "alteration";
    public static final String PERCENT_ALTERED = "percent_altered";
    public static final String MUTATION = "mutation";


    private static Log log = LogFactory.getLog(GeneDataJSON.class);

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
     * todo: this is code duplication!
     * Format percentage.
     *
     * <p/>
     * if value == 0 return "--"
     * case value
     * 0: return "--"
     * 0<value<=0.01: return "<1%"
     * 1<value: return "<value>%"
     *
     * @param value double
     *
     * @return String
     */
    public static String alterationValueToString(double value) {

        // in oncoPrint show 0 percent as 0%, not --
        if (0.0 < value && value <= 0.01) {
            return "<1%";
        }

        // if( 1.0 < value ){
        Formatter f = new Formatter();
        f.format("%.0f", value * 100.0);
        return f.out().toString() + "%";
    }

    /**
     * Maps the matrix to a JSONArray of alterations
     * @param geneticEvents matrix M[case][gene]
     * @return
     */
    public JSONObject mapGeneticEventMatrix(GeneticEvent geneticEvents[][], ProfileDataSummary dataSummary)
            throws ServletException {

        JSONArray genes = new JSONArray();
        JSONObject hugo_to_index = new JSONObject();
        JSONObject samples = new JSONObject();

        // get all caseIds and put them in an array
        for (int j = 0; j < geneticEvents[0].length; j++) {
            String caseId = geneticEvents[0][j].caseCaseId();
            samples.put(caseId, j);
        }

        // for each gene, get the data and put it into an array
        for (int i = 0; i < geneticEvents.length; i++) {
            GeneticEvent rowEvent = geneticEvents[i][0];
            String gene = rowEvent.getGene().toUpperCase();
            String percent_altered =
                    alterationValueToString(dataSummary.getPercentCasesWhereGeneIsAltered(rowEvent.getGene()));

            JSONArray mutation = new JSONArray();
            JSONArray cna = new JSONArray();
            JSONArray mrna = new JSONArray();
            JSONArray rppa = new JSONArray();

            for (int j = 0; j < geneticEvents[0].length; j++) {

                GeneticEvent event = geneticEvents[i][j];

//                System.out.println("GeneAlterations caseId: " + event.caseCaseId() + ", event: " + event);

                String sample_cna = event.getCnaValue().name().toUpperCase();
                String sample_mrna = event.getMrnaValue().name().toUpperCase();
                String sample_rppa = event.getRPPAValue().name().toUpperCase();
                String sample_mutation = event.getMutationType();
                
                mutation.add(event.isMutated() ? sample_mutation : null);
                cna.add(sample_cna.equals("NONE") ? null : sample_cna);
                mrna.add(sample_mrna.equals("NOTSHOWN") ? null : sample_mrna);
                rppa.add(sample_rppa.equals("NOTSHOWN") ? null : sample_rppa);
            }

            hugo_to_index.put(gene, i);

            JSONObject gene_data = new JSONObject();
            gene_data.put("hugo", gene);            // efficiency at the price of redundancy
            gene_data.put("percent_altered", percent_altered);
            gene_data.put("mutations", mutation);
            gene_data.put("cna", cna);
            gene_data.put("mrna", mrna);
            gene_data.put("rppa", rppa);

            genes.add(gene_data);
        }
        
        JSONObject data = new JSONObject();
        data.put("samples", samples);
        data.put("hugo_to_gene_index", hugo_to_index);
        data.put("gene_data", genes);

        return data;
    }

    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String _geneList = request.getParameter("genes");
        // list of genes separated by a space

        String sampleIds = request.getParameter("samples");
        // list of samples separated by a space.  This is so
        // that you can query by an arbitrary set of samples
        // separated by a space

        String _geneticProfileIds = request.getParameter("geneticProfileIds");
        // list of geneticProfileIds separated by a space
        // e.g. gbm_mutations, gbm_cna_consensus

        HashSet<String> geneticProfileIdSet = new HashSet<String>(Arrays.asList(_geneticProfileIds.split(" ")));

        // map geneticProfileIds -> geneticProfiles
        Iterator<String> gpSetIterator =  geneticProfileIdSet.iterator();
        ArrayList<GeneticProfile> profileList = new ArrayList<GeneticProfile>();
        while (gpSetIterator.hasNext()) {
            String gp_str = gpSetIterator.next();

            GeneticProfile gp = DaoGeneticProfile.getGeneticProfileByStableId(gp_str);
            profileList.add(gp);
            // pointer to gp is local, but gets added to profileList which is outside
        }

        // todo: how should this *not* be hard coded?
        double zScoreThreshold = ZScoreUtil.Z_SCORE_THRESHOLD_DEFAULT;
        double rppaScoreThreshold = ZScoreUtil.RPPA_SCORE_THRESHOLD_DEFAULT;

        // ... do a bunch of work to get the matrix, basically copying out of QueryBuilder ...
        // todo: this is code duplication!
        ParserOutput theOncoPrintSpecParserOutput =
                OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(_geneList,
                        geneticProfileIdSet, profileList, zScoreThreshold, rppaScoreThreshold);

        ArrayList<String> listOfGenes =
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes();
        String[] listOfGeneNames = new String[listOfGenes.size()];
        listOfGeneNames = listOfGenes.toArray(listOfGeneNames);

        ArrayList<ProfileData> profileDataList = new ArrayList<ProfileData>();
        Iterator<String> profileIterator = geneticProfileIdSet.iterator();

        XDebug xdebug = new XDebug(request);
        while (profileIterator.hasNext()) {
            String profileId = profileIterator.next();
            GeneticProfile profile = GeneticProfileUtil.getProfile(profileId, profileList);
            if( null == profile ){
                continue;
            }

            xdebug.logMsg(this, "Getting data for:  " + profile.getProfileName());

            GetProfileData remoteCall;
            try {
                remoteCall = new GetProfileData(profile, listOfGenes, sampleIds);
            } catch (DaoException e) {
                throw new ServletException(e);
            }
            ProfileData pData = remoteCall.getProfileData();
            if(pData == null){
                System.err.println("pData == null");
            } else {
                if (pData.getGeneList() == null ) {
                    System.err.println("pData.getValidGeneList() == null");
                } if (pData.getCaseIdList().size() == 0) {
                    System.err.println("pData.length == 0");
                }
            }
            if (pData != null) {
                xdebug.logMsg(this, "Got number of genes:  " + pData.getGeneList().size());
                xdebug.logMsg(this, "Got number of cases:  " + pData.getCaseIdList().size());
            }
            xdebug.logMsg(this, "Number of warnings received:  " + remoteCall.getWarnings().size());
            profileDataList.add(pData);
        }

        xdebug.logMsg(this, "Merging Profile Data");
        ProfileMerger merger = new ProfileMerger(profileDataList);
        ProfileData mergedProfile = merger.getMergedProfile();

        ProfileDataSummary dataSummary = new ProfileDataSummary(mergedProfile,
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold, rppaScoreThreshold);

        GeneticEvent geneticEvents[][] = ConvertProfileDataToGeneticEvents.convert
                (dataSummary, listOfGeneNames,
                        theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold, rppaScoreThreshold);

        // out.write the matrix

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        JSONObject geneticEventsJSON = mapGeneticEventMatrix(geneticEvents, dataSummary);

        // get outa here!
        JSONObject.writeJSONString(geneticEventsJSON, out);
    }

    /**
     * Just in case the request changes from GET to POST
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        doPost(request, response);
    }
}
