/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.util.DatabaseProperties;
import org.mskcc.cbio.portal.util.WebserviceParserUtils;
import org.mskcc.cbio.portal.web_api.*;

/**
 * Core Web Service.
 *
 * @author Ethan Cerami.
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class WebService extends HttpServlet {

    public static final String CANCER_STUDY_ID = "cancer_study_id";
    public static final String CANCER_TYPE_ID = "cancer_type_id";
    public static final String GENETIC_PROFILE_ID = "genetic_profile_id";
    public static final String GENE_LIST = "gene_list";
    public static final String CMD = "cmd";
    public static final String Q_VALUE_THRESHOLD = "q_value_threshold";
    public static final String GENE_SYMBOL = "gene_symbol";
    public static final String ENTREZ_GENE_ID = "entrez_gene_id";
    public static final String CASE_LIST = "case_list";
    public static final String CASE_IDS_KEY = "case_ids_key";
    public static final String CASE_SET_ID = "case_set_id";
    public static final String SUPPRESS_MONDRIAN_HEADER = "suppress_mondrian_header";
    public static final String EMAIL_ADDRESS = "email_address";
    public static final String SECRET_KEY = "secret_key";
    public static final String PROTEIN_ARRAY_TYPE = "protein_array_type";
    public static final String PROTEIN_ARRAY_ID = "protein_array_id";
    public static final String FORMAT = "format";

    /**
     * Shutdown the Servlet.
     */
    public void destroy() {
        super.destroy();
    }

    /**
     * Initializes Servlet with parameters in web.xml file.
     *
     * @throws javax.servlet.ServletException Servlet Initialization Error.
     */
    public void init() throws ServletException {
        super.init();
        System.out.println("Starting up the Web Service API...");
        System.out.println("Reading in init parameters from web.xml");
        DatabaseProperties dbProperties = DatabaseProperties.getInstance();
        ServletConfig config = this.getServletConfig();
        String dbHost = config.getInitParameter("db_host");
        String dbUser = config.getInitParameter("db_user");
        String dbPassword = config.getInitParameter("db_password");
        String dbName = config.getInitParameter("db_name");
        System.out.println("Starting CGDS Server");
        dbProperties.setDbName(dbName);
        dbProperties.setDbHost(dbHost);
        dbProperties.setDbUser(dbUser);
        dbProperties.setDbPassword(dbPassword);
        verifyDbConnection();
    }

    /**
     * Handles GET Requests.
     *
     * @param httpServletRequest  HttpServlet Request.
     * @param httpServletResponse HttpServlet Response.
     * @throws ServletException Servlet Error.
     * @throws IOException      IO Error.
     */
    @Override
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException, IOException {
        processClient(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles POST Requests.
     *
     * @param httpServletRequest  HttpServlet Request.
     * @param httpServletResponse HttpServlet Response.
     * @throws ServletException Servlet Error.
     * @throws IOException      IO Error.
     */
    @Override
    protected void doPost(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse) throws ServletException, IOException {
        processClient(httpServletRequest, httpServletResponse);
    }

    /**
     * Processes all Client Requests.
     *
     * @param httpServletRequest  HttpServlet Request.
     * @param httpServletResponse HttpServlet Response.
     * @throws IOException IO Error.
     */
    public void processClient(HttpServletRequest httpServletRequest,
                              HttpServletResponse httpServletResponse) throws IOException {
        PrintWriter writer = httpServletResponse.getWriter();
        Date startTime = new Date();
        String cmd = httpServletRequest.getParameter(CMD);


        try {
            // set the content type based on the format parameter
            if ("json".equals(httpServletRequest.getParameter(FORMAT))) {
                httpServletResponse.setContentType("application/json");
            }
            else {
                httpServletResponse.setContentType("text/plain");
            }

            // Branch, based on command.
            if (null == cmd) {
                outputMissingParameterError(writer, CMD);
                return;
            }

            // check command
            if (!goodCommand(writer, cmd)) {
                return;
            }

            if (cmd.equals("getTypesOfCancer")) {
                getTypesOfCancer(writer);
                return;
            }
            if (cmd.equals("getNetwork")) {
                getNetwork(httpServletRequest, writer);
                return;
            }

            if (cmd.equals("getProteinArrayInfo")) {
                getProteinArrayInfo(httpServletRequest, writer);
                return;
            }
            if (cmd.equals("getProteinArrayData")) {
                getProteinArrayData(httpServletRequest, writer);
                return;
            }
            if (cmd.equals("getMutSig")) {
                getMutSig(httpServletRequest, writer);
                return;
            }

            //  We support the new getCancerStudies plus the deprecated getCancerTypes command
            if (cmd.equals("getCancerStudies") || cmd.equals("getCancerTypes")) {
                getCancerStudies(httpServletRequest, writer);
                return;
            }


            // TODO: CASES: REMOVE?
            // no cancer_study_id or no case_set_id or genetic_profile_id
            if (null == WebserviceParserUtils.getCancerStudyId(httpServletRequest) &&
                    null == httpServletRequest.getParameter(WebService.CASE_SET_ID) &&
                    null == httpServletRequest.getParameter(WebService.GENETIC_PROFILE_ID) &&
                    null == httpServletRequest.getParameter(WebService.CASE_LIST)) {
                outputError(writer, "No cancer study (cancer_study_id), or genetic profile (genetic_profile_id) " +
                        "or case list or (case_list) case set (case_set_id) provided by request. " +
                        "Please reformulate request.");
                return;
            }

            HashSet<String> cancerStudyIDs = WebserviceParserUtils.getCancerStudyIDs(httpServletRequest);
            if (cancerStudyIDs.isEmpty()) {
                outputError(writer, "Problem when identifying a cancer study for the request.");
                return;
            }
            // TODO: if cancerStudyID == CancerStudy.NO_SUCH_STUDY report an error with more info
            for (String cancerStudyID : cancerStudyIDs) {
                if (!DaoCancerStudy.doesCancerStudyExistByStableId(cancerStudyID)) {
                    outputError(writer, "The cancer study identified by the request (" + cancerStudyID +
                            ") is not in the dbms. Please reformulate request.");
                    return;
                }
            }

            if (cmd.equals("getGeneticProfiles")) {
                // PROVIDES CANCER_STUDY_ID
                getGeneticProfiles(httpServletRequest, writer);
            } else if (cmd.equals("getProfileData")) {
                // PROVIDES genetic_profile_id
                getProfileData(httpServletRequest, writer);
            } else if (cmd.equals("getCaseLists")) {
                // PROVIDES CANCER_STUDY_ID
                getCaseLists(httpServletRequest, writer);
            } else if (cmd.equals("getClinicalData")) {
                // PROVIDES case_set_id
                getClinicalData(httpServletRequest, writer);
            } else if (cmd.equals("getMutationData")) {
                // PROVIDES genetic_profile_id
                getMutationData(httpServletRequest, writer);
            } else if (cmd.equals("getMutationFrequency")) {
                // PROVIDES CANCER_STUDY_ID
                getMutationFrequency(httpServletRequest, writer);
            } else {
                throw new ProtocolException("Unrecognized command: " + cmd);
            }
        } catch (DaoException e) {
            e.printStackTrace();
            outputError(writer, "internal error:  " + e.getMessage());
        } catch (ProtocolException e) {
            e.printStackTrace();
            outputError(writer, e.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            outputError(writer, e.toString());
        } finally {
            writer.flush();
            writer.close();
            Date stopTime = new Date();
            long timeElapsed = stopTime.getTime() - startTime.getTime();
        }
    }

    /**
     * Gets the Network of Interest.
     *
     * @param httpServletRequest HttpServletRequest Object.
     * @param writer             Print Writer Object.
     * @throws DaoException      Database Exception.
     * @throws ProtocolException Protocol Exception.
     */
    private void getNetwork(HttpServletRequest httpServletRequest,
                            PrintWriter writer) throws DaoException, ProtocolException {
        String geneList = httpServletRequest.getParameter(GENE_LIST);
        if (geneList == null || geneList.length() == 0) {
            throw new ProtocolException("Missing Parameter:  " + GENE_LIST);
        }
        ArrayList<String> targetGeneList = getGeneList(httpServletRequest);
        String out = GetNetwork.getNetwork(targetGeneList);
        writer.print(out);
    }

    private void getProteinArrayInfo(HttpServletRequest httpServletRequest,
                                     PrintWriter writer) throws DaoException, ProtocolException {
        String cancerStudyId = WebserviceParserUtils.getCancerStudyId(httpServletRequest);
        if (cancerStudyId == null) {
            outputMissingParameterError(writer, CANCER_STUDY_ID);
            return;
        }
        
        String geneList = httpServletRequest.getParameter(GENE_LIST);
        ArrayList<String> targetGeneList;
        if (geneList == null || geneList.length() == 0) {
            targetGeneList = null;
        } else {
            targetGeneList = getGeneList(httpServletRequest);
        }

        String type = httpServletRequest.getParameter(PROTEIN_ARRAY_TYPE);
        writer.print(GetProteinArrayData.getProteinArrayInfo(cancerStudyId, targetGeneList, type));
    }

    private void getProteinArrayData(HttpServletRequest httpServletRequest,
                                     PrintWriter writer) throws DaoException, ProtocolException {
        String arrayId = httpServletRequest.getParameter(PROTEIN_ARRAY_ID);
        String cancerStudyId = null;
        if (arrayId == null || arrayId.length() == 0) {
            cancerStudyId = WebserviceParserUtils.getCancerStudyIDs(httpServletRequest).iterator().next();
        }
        ArrayList<String> targetCaseIds = null;
        if (null != httpServletRequest.getParameter(CASE_LIST)
        		|| null != httpServletRequest.getParameter(CASE_SET_ID)
        		|| null != httpServletRequest.getParameter(CASE_IDS_KEY))
            targetCaseIds = WebserviceParserUtils.getCaseList(httpServletRequest);
        
        String arrayInfo = httpServletRequest.getParameter("array_info");
        boolean includeArrayInfo = arrayInfo!=null && arrayInfo.equalsIgnoreCase("1");
        writer.print(GetProteinArrayData.getProteinArrayData(cancerStudyId, 
                arrayId==null?null : Arrays.asList(arrayId.split("[ ,]+")), 
                targetCaseIds, includeArrayInfo));
    }

    private void getTypesOfCancer(PrintWriter writer) throws DaoException, ProtocolException {

        String out = GetTypesOfCancer.getTypesOfCancer();
        writer.print(out);
    }

    private void getCancerStudies(HttpServletRequest httpServletRequest, PrintWriter writer) 
            throws DaoException, ProtocolException {
        String out = GetTypesOfCancer.getCancerStudies();
        writer.print(out);
    }

    private void getMutationFrequency(HttpServletRequest httpServletRequest, PrintWriter writer)
            throws DaoException, ProtocolException {

        String cancerStudyId = WebserviceParserUtils.getCancerStudyId(httpServletRequest);
        if (cancerStudyId == null) {
            outputMissingParameterError(writer, CANCER_STUDY_ID);
        } else {
            String out = GetMutationFrequencies.getMutationFrequencies(Integer.parseInt(cancerStudyId),
                    httpServletRequest);
            writer.print(out);
        }
    }

    private void getGeneticProfiles(HttpServletRequest httpServletRequest, PrintWriter writer)
            throws DaoException {

        String cancerStudyStableId = WebserviceParserUtils.getCancerStudyId(httpServletRequest);
        if (cancerStudyStableId == null) {
            outputMissingParameterError(writer, CANCER_STUDY_ID);
        } else {
            String out = GetGeneticProfiles.getGeneticProfilesAsTable(cancerStudyStableId);
            writer.print(out);
        }
    }

    private void getCaseLists(HttpServletRequest httpServletRequest, PrintWriter writer)
            throws DaoException {

        String cancerStudyStableId = WebserviceParserUtils.getCancerStudyId(httpServletRequest);
        if (cancerStudyStableId == null) {
            outputMissingParameterError(writer, CANCER_STUDY_ID);
        } else {
            String out = GetCaseLists.getCaseListsAsTable(cancerStudyStableId);
            writer.print(out);
        }
    }

    private void outputError(PrintWriter writer, String msg) {
        writer.print("Error: " + msg + "\n");
    }

    private void getProfileData(HttpServletRequest request, PrintWriter writer)
            throws DaoException, ProtocolException, IOException {
        ArrayList<String> caseList = WebserviceParserUtils.getCaseList(request);
        validateRequestForProfileOrMutationData(request);
        ArrayList<String> geneticProfileIdList = WebserviceParserUtils.getGeneticProfileId(request);
        ArrayList<String> targetGeneList = getGeneList(request);

        if (targetGeneList.size() > 1 && geneticProfileIdList.size() > 1) {
            throw new ProtocolException
                    ("You can specify multiple genes or multiple genetic profiles, " +
                            "but not both at once!");
        }

        Boolean suppressMondrianHeader = Boolean.parseBoolean(request.getParameter(SUPPRESS_MONDRIAN_HEADER));
        GetProfileData getProfileData = new GetProfileData(geneticProfileIdList, targetGeneList,
                caseList, suppressMondrianHeader);

        String format = WebserviceParserUtils.getFormat(request);

        if (format == null || "txt".equals(format.toLowerCase())) {
            // default to txt if format parameter is not specified
            String out = getProfileData.getRawContent();
            writer.print(out);
        }
        else if ("json".equals(format.toLowerCase())) {
            JSONArray.writeJSONString(getProfileData.getJson(), writer);
        }
    }

    private void getClinicalData(HttpServletRequest request, PrintWriter writer)
            throws DaoException, ProtocolException, IOException {

        String cancerStudyId = WebserviceParserUtils.getCancerStudyId(request);
        if (cancerStudyId==null) {
            Set<String> cancerStudyIds = WebserviceParserUtils.getCancerStudyIDs(request);
            if (cancerStudyIds.size()!=1) {
                writer.print("The cmd only support one and only one cancer study.");
                return;
            }
            cancerStudyId = cancerStudyIds.iterator().next();
        }

        List<String> caseIds = WebserviceParserUtils.getCaseList(request);

        String format = WebserviceParserUtils.getFormat(request);

        String attrId = request.getParameter("attribute_id");

        if (format == null || "txt".equals(format)) { // default to txt if format parameter is not specified
            if (attrId == null) {
                writer.print(GetClinicalData.getTxt(cancerStudyId, caseIds));
            } else {
                if (caseIds.size() != 1) {
                    throw new IOException("cannot ask for multiple cases");
                }
                writer.print(GetClinicalData.getTxtDatum(cancerStudyId, caseIds.get(0), attrId));
            }
        }
        else if ("json".equals(format)) {
            if (attrId == null) {
                JSONObject.writeJSONString(GetClinicalData.getJSON(cancerStudyId, caseIds), writer);
            } else {
                JSONObject outObject;
                if (caseIds.size() == 1) {
                    outObject = GetClinicalData.getJsonDatum(cancerStudyId, caseIds.get(0), attrId);
                }
                else {
                    outObject = GetClinicalData.getJSON(cancerStudyId, caseIds, attrId);
                }
                JSONObject.writeJSONString(outObject, writer);
            }
        }
        else {
            // die
            writer.print("There was an error in processing your request.  Please try again");
            throw new ProtocolException("please specify the format, i.e. format=txt OR format=json");
        }
    }

    /*
     * For getMutSig client specifies a Cancer Study ID,
     * and either a q_value_threshold, or a gene list.
     * The two latter parameters are optional.
     */
    private void getMutSig(HttpServletRequest request, PrintWriter writer)
               throws DaoException {
           String cancerStudyID = WebserviceParserUtils.getCancerStudyId(request);
           if ((cancerStudyID == null) || (cancerStudyID.length() == 0)) {
               writer.print("Please enter a Cancer Type");
           }
           String qValueThreshold = request.getParameter(Q_VALUE_THRESHOLD);
           String geneList = request.getParameter(GENE_LIST);
           CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyID);
           int cancerID = cancerStudy.getInternalId();
           if (qValueThreshold == null && geneList == null) {
               StringBuffer output = GetMutSig.getMutSig(cancerID);
               writer.print(output);
           } else if (qValueThreshold != null && geneList == null) {
               StringBuffer output = GetMutSig.getMutSig(cancerID, qValueThreshold, true);
               writer.print(output);
           } else if (qValueThreshold == null && geneList != null) {
               StringBuffer output = GetMutSig.getMutSig(cancerID, geneList, false);
               writer.print(output);
           } else {
               writer.print("Invalid command. Please input a valid Q-Value Threshold, or Gene List.");
           }
       }

    private void getMutationData(HttpServletRequest request, PrintWriter writer)
            throws DaoException, ProtocolException, UnsupportedEncodingException {
        ArrayList<String> caseList = null;
        try {
            caseList = WebserviceParserUtils.getCaseList(request);
        } catch (ProtocolException ex) {}
        validateRequestForProfileOrMutationData(request);
        ArrayList<String> geneticProfileIdList = WebserviceParserUtils.getGeneticProfileId(request);
        
        for (String geneticProfileId : geneticProfileIdList) {
            ArrayList<String> targetGeneList = getGeneList(request);
            String out = GetMutationData.getProfileData(geneticProfileId, targetGeneList,
                    caseList);
            writer.print(out);
        }
    }

    private ArrayList<String> getGeneList(HttpServletRequest request) {
        String geneList = request.getParameter(GENE_LIST);
        //  Split on white space or commas
        Pattern p = Pattern.compile("[,\\s]+");
        String genes[] = p.split(geneList);
        ArrayList<String> targetGeneList = new ArrayList<String>();
        for (String gene : genes) {
            gene = gene.trim();
            if (gene.length() == 0) {
                continue;
            }
            targetGeneList.add(gene);
        }
        return targetGeneList;
    }

    private void validateRequestForProfileOrMutationData(HttpServletRequest request)
            throws ProtocolException {
        String geneticProfileIdStr = request.getParameter(GENETIC_PROFILE_ID);
        String geneList = request.getParameter(GENE_LIST);
        if (geneticProfileIdStr == null || geneticProfileIdStr.length() == 0) {
            throw new ProtocolException("Missing Parameter:  " + GENETIC_PROFILE_ID);
        }

        if (geneList == null || geneList.length() == 0) {
            throw new ProtocolException("Missing Parameter:  " + GENE_LIST);
        }
    }

    private void outputMissingParameterError(PrintWriter writer, String missingParameter) {
        outputError(writer, "you must specify a " + missingParameter + " parameter.");
    }

    /**
     * Verifies Database Connection.  In the event of an error, log
     * messages are written out to catalina.out.
     */
    private void verifyDbConnection() {
        if (DaoCancerStudy.getCount()==0) {
            System.err.println("****  Fatal Error in CGDS.  Could not connect to "
                    + "database");
        }
    }

    private boolean goodCommand(PrintWriter writer, String cmd) {
        // check that command is correct
        String[] commands = {"getTypesOfCancer", "getNetwork", "getCancerStudies",
                "getCancerTypes", "getGeneticProfiles", "getProfileData", "getCaseLists",
                "getClinicalData", "getMutationData", "getMutationFrequency",
                "getProteinArrayInfo", "getProteinArrayData", "getMutSig"};
        for (String aCmd : commands) {
            if (aCmd.equals(cmd)) {
                return true;
            }
        }
        outputError(writer, "'" + cmd + "' not a valid command.");
        return false;

    }
}
