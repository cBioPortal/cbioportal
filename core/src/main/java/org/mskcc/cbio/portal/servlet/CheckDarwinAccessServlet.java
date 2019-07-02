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

import org.mskcc.cbio.portal.util.SpringUtil;
import org.mskcc.cbio.portal.util.GlobalProperties;
import org.apache.log4j.Logger;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import org.apache.commons.cli.*;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.*;
import java.util.*;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.*;
import java.util.regex.Pattern;

import javax.servlet.http.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 *
 * @author ochoaa
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
"darwinAuthResponse",
"p_userName",
"p_dmp_pid",
"deidentification_id"
})
public class CheckDarwinAccessServlet extends HttpServlet {
    private static Logger logger = Logger.getLogger(CheckDarwinAccessServlet.class);
    private static final String DDP_INFO_ENDPOINT = "/info";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
                config.getServletContext());
    }
    
    public static class CheckDarwinAccess {
        private static String darwinAuthUrl = GlobalProperties.getDarwinAuthCheckUrl();
        private static String ddpResponseUrl = GlobalProperties.getDdpResponseUrl();
        private static String cisUser = GlobalProperties.getCisUser();
        public static Pattern sampleIdRegex = Pattern.compile(GlobalProperties.getDarwinRegex());

        public static String checkAccess(HttpServletRequest request) {
            if (!existsDarwinProperties()) return "";
            // if sample id does not match regex or username matches cis username then return empty string
            String userName = GlobalProperties.getAuthenticatedUserName().split("@")[0];
            String darwinResponse = "";
            try {
                String[] sampleIds = request.getParameter(PatientView.SAMPLE_ID).split(",");
                if (sampleIdRegex.matcher(sampleIds[0]).find() && !cisUser.equals(userName)) {
                    String patientId = request.getParameter(PatientView.PATIENT_ID);
                    darwinResponse = getResponse(userName, patientId);
                }
            }
            catch (NullPointerException ex) {}
            
			return darwinResponse;
        }
        
        public static String getResponse(String userName, String patientId){
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = getRequestEntity(userName, patientId);
            ResponseEntity<DarwinAccess> responseEntity = restTemplate.exchange(darwinAuthUrl, HttpMethod.POST, requestEntity, DarwinAccess.class);
            String darwinResponse = responseEntity.getBody().getDarwinAuthResponse();
            String deidentificationId = responseEntity.getBody().getDeidentification_Id();
            if (!darwinResponse.equals("valid")) {
                return "";
            }
            if (deidentificationId.isEmpty()) {
                return "";
            }
            // construct URL 
            return ddpResponseUrl + deidentificationId + DDP_INFO_ENDPOINT;
        }

        private static HttpEntity<LinkedMultiValueMap<String, Object>> getRequestEntity(String userName, String patientId) {
            LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            map.add("p_userName", userName);
            map.add("p_dmp_pid", patientId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            return new HttpEntity<LinkedMultiValueMap<String, Object>>(map, headers);
        }
        
        public static boolean existsDarwinProperties() {
            return (!darwinAuthUrl.isEmpty() && !ddpResponseUrl.isEmpty() && !cisUser.isEmpty() && !GlobalProperties.getDarwinRegex().isEmpty());
        }
    }   

    public static class DarwinAccess {
        /**
        * (Required)
        **/
        @JsonProperty("darwinAuthResponse")
        private String darwinAuthResponse;
        /**
        * (Required)
        **/
        @JsonProperty("p_userName")
        private String p_userName;
        /**
        * (Required)
        **/
        @JsonProperty("p_dmp_pid")
        private String p_dmp_pid;
        /**
        * (Required)
        **/
        @JsonProperty("deidentification_id")
        private String deidentification_id;

        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
        * No args constructor for use in serialization
        **/
        public DarwinAccess() {}

        /**
        * @param darwinAuthResponse
        * @param p_userName
        * @param p_dmp_pid
        * @param deidentification_id
        **/
        public DarwinAccess(String darwinAuthResponse, String p_userName, String p_dmp_pid, String deidentification_id) {
            this.darwinAuthResponse = darwinAuthResponse;
            this.p_userName = p_userName;
            this.p_dmp_pid = p_dmp_pid;
            this.deidentification_id = deidentification_id;
        }

        /**
        * (Required)
        * @return
        * The darwinAuthResponse
        **/
        @JsonProperty("darwinAuthResponse")
        public String getDarwinAuthResponse() {
            return darwinAuthResponse;
        }

        /**
        * (Required)
        * @param darwinAuthResponse
        * The Darwin authorization response
        **/
        @JsonProperty("darwinAuthResponse")
        public void setDarwinAuthResponse(String darwinAuthResponse) {
            this.darwinAuthResponse = darwinAuthResponse;
        }

        public DarwinAccess withDarwinAuthResponse(String darwinAuthResponse) {
            this.darwinAuthResponse = darwinAuthResponse;
            return this;
        }

        /**
        * (Required)
        * @return
        * The p_userName
        **/
        @JsonProperty("p_userName")
        public String getP_UserName() {
            return p_userName;
        }

        /**
        * (Required)
        * @param p_userName
        * The p_userName
        **/
        @JsonProperty("p_userName")
        public void setP_UserName(String p_userName) {
            this.p_userName = p_userName;
        }

        public DarwinAccess withP_UserName(String p_userName) {
            this.p_userName = p_userName;
            return this;
        }

        /**
        * (Required)
        * @return
        * The p_dmp_pid
        **/
        @JsonProperty("p_dmp_pid")
        public String getP_Dmp_Pid() {
            return p_dmp_pid;
        }

        /**
        * (Required)
        * @param p_dmp_pid
        * The p_dmp_pid
        **/
        @JsonProperty("p_dmp_pid")
        public void setP_Dmp_Pid(String p_dmp_pid) {
            this.p_dmp_pid = p_dmp_pid;
        }

        /**
        * (Required)
        * @return
        * The deidentification_id
        **/
        @JsonProperty("deidentification_id")
        public String getDeidentification_Id() {
            return deidentification_id;
        }

        /**
        * (Required)
        * @param deidentification_id
        * The deidentification_id
        **/
        @JsonProperty("deidentification_id")
        public void setDeidentification_Id(String deidentification_id) {
            this.deidentification_id = deidentification_id;
        }

        public DarwinAccess withP_Dmp_Pid(String p_dmp_pid) {
            this.p_dmp_pid = p_dmp_pid;
            return this;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

        public DarwinAccess withAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
            return this;
        }
    }    
    
    private static Options getOptions(String[] args) {
        Options gnuOptions = new Options();
        gnuOptions.addOption("h", "help", false, "shows this help document and quits.")
            .addOption("u", "user_name", true, "Username")
            .addOption("p", "patient_id", true, "Patient ID")
            .addOption("s", "sample_id", true, "Sample ID");

        return gnuOptions;
    }
    private static void help(Options gnuOptions, int exitStatus) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("CheckDarwinAccess", gnuOptions);
        System.exit(exitStatus);
    }    

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

		String darwinResponse = CheckDarwinAccess.checkAccess(request);

		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		try {
			out.write(darwinResponse);
		} finally {            
			out.close();
		}
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
