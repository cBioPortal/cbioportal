package org.mskcc.cbio.portal.servlet;


import com.google.common.base.Joiner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.WebserviceParserUtils;
import org.mskcc.cbio.portal.web_api.GetProfileData;
import org.mskcc.cbio.portal.web_api.ProtocolException;
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
    }

    /**
     * Maps the matrix to a JSONArray of alterations
     *
     *
     * todo: This is replacing the commented out code below.  Soon we the old version below?
     * @param geneticEvents matrix M[case][gene]
     * @return
     */
    public JSONArray mapGeneticEventMatrix(GeneticEvent geneticEvents[][]) {
        JSONArray data = new JSONArray();

        for (int i = 0; i < geneticEvents.length; i++) {
            for (int j = 0; j < geneticEvents[0].length; j++) {
                JSONObject datum = new JSONObject();
                datum.put("sample", geneticEvents[i][j].caseCaseId());
                datum.put("gene", geneticEvents[i][j].getGene());

                GeneticEvent event = geneticEvents[i][j];
                String cna = event.getCnaValue().name().toUpperCase();
                if (!cna.equals(GeneticEventImpl.CNA.NONE.toString())) {
                    datum.put("cna", cna);
                }

                String mrna = event.getMrnaValue().name().toUpperCase();
                if (!mrna.equals(GeneticEventImpl.MRNA.NOTSHOWN.toString())) {
                    datum.put("mrna", mrna);
                }

                String rppa = event.getRPPAValue().name().toUpperCase();
                if (!rppa.equals(GeneticEventImpl.RPPA.NOTSHOWN.toString())) {
                    datum.put("rppa", rppa);
                }

                if (event.isMutated()) {
                    datum.put("mutation",  event.getMutationType());
                }

                data.add(datum);
            }
        }

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

        // OncoQuery Language string
        String oql = request.getParameter("oql");

	    if (request instanceof XssRequestWrapper)
	    {
		    oql = ((XssRequestWrapper)request).getRawParameter("oql");
	    }

        oql = oql.replaceAll("\n", " \n ");

        String sampleIds;
        // list of samples separated by a space.  This is so
        // that you can query an arbitrary set of samples
        // separated by a space

        try {
            List<String> caseIds = WebserviceParserUtils.getCaseList(request);
            sampleIds = Joiner.on(" ").join(caseIds);
        } catch (ProtocolException e) {
            throw new ServletException(e);
        } catch (DaoException e) {
            throw new ServletException(e);
        }

        String _geneticProfileIds = request.getParameter("geneticProfileIds");
        // list of geneticProfileIds separated by a space
        // e.g. gbm_mutations, gbm_cna_consensus

        HashSet<String> geneticProfileIdSet = new HashSet<String>(Arrays.asList(_geneticProfileIds.trim().split(" ")));

        // map geneticProfileIds -> geneticProfiles
        Iterator<String> gpSetIterator =  geneticProfileIdSet.iterator();
        ArrayList<GeneticProfile> profileList = new ArrayList<GeneticProfile>();
        while (gpSetIterator.hasNext()) {
            String gp_str = gpSetIterator.next();

            GeneticProfile gp = DaoGeneticProfile.getGeneticProfileByStableId(gp_str);
            profileList.add(gp);
            // pointer to gp is local, but gets added to profileList which is outside
        }

        double zScoreThreshold = Double.valueOf(request.getParameter("z_score_threshold"));
        double rppaScoreThreshold = Double.valueOf(request.getParameter("rppa_score_threshold"));

        // todo: this is code duplication!
        // this is a duplication of work that is being done in QueryBuilder.
        // For now, we cannot remove it from QueryBuilder because other parts use it...for now
        // ...this is a temporary solution
        ParserOutput theOncoPrintSpecParserOutput =
                OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(oql,
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

        JSONArray jsonArray = mapGeneticEventMatrix(geneticEvents);
        JSONArray.writeJSONString(jsonArray, out);
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
