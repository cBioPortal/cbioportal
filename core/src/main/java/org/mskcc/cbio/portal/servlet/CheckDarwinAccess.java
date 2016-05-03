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

import org.mskcc.cbio.portal.model.DarwinAccess;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;

import org.apache.commons.cli.*;
import org.mskcc.cbio.portal.util.GlobalProperties;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author ochoaa
 */
public class CheckDarwinAccess {
    
    public static String checkAccess(String userName, String patientId){
        RestTemplate restTemplate = new RestTemplate();                 
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = getRequestEntity(userName.split("@")[0], patientId);  
        ResponseEntity<DarwinAccess> responseEntity = restTemplate.exchange(GlobalProperties.getDarwinAuthUrlBase(), HttpMethod.POST, requestEntity, DarwinAccess.class);  
        DarwinAccess response = responseEntity.getBody();            
        
        if (response.getDarwinAuthResponse().equals("valid")) {
            return GlobalProperties.getDarwinResponseUrlBase()+patientId;
        }
        else {
            System.out.println(response);
            System.out.println("\nAuthorization\tUserName\tPatientId:");
            System.out.println(response.getDarwinAuthResponse()+"\t"+
                    response.getP_UserName()+"\t"+
                    response.getP_Dmp_Pid());
            return "";            
        }
    }
    
    private static HttpEntity<LinkedMultiValueMap<String, Object>> getRequestEntity(String userName, String patientId) {
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("p_userName", userName);
        map.add("p_dmp_pid", patientId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<LinkedMultiValueMap<String, Object>>(map, headers);
    }

    private static Options getOptions(String[] args) {
        Options gnuOptions = new Options();
        gnuOptions.addOption("h", "help", false, "shows this help document and quits.")
            .addOption("user_name", "user_name", true, "user name")
            .addOption("patient_id", "patient_id", true, "patient_id");

        return gnuOptions;
    }
    private static void help(Options gnuOptions, int exitStatus) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("CheckDarwinAccess", gnuOptions);
        System.exit(exitStatus);
    }    
    
    public static void main(String[] args) throws Exception {
        Options gnuOptions = CheckDarwinAccess.getOptions(args);
        CommandLineParser parser = new GnuParser();
        CommandLine commandLine = parser.parse(gnuOptions, args);
        if (commandLine.hasOption("h") ||
            !commandLine.hasOption("user_name") ||
            !commandLine.hasOption("patient_id")) {
            help(gnuOptions, 0);
        }
        
        String darwinAccessUrl = checkAccess(commandLine.getOptionValue("user_name"),
                  commandLine.getOptionValue("patient_id"));
        if (darwinAccessUrl != null){
            System.out.println(darwinAccessUrl);
        }
        else {
            System.out.println("User "+commandLine.getOptionValue("user_name")+
                    " does not have access to patient "+commandLine.getOptionValue("patient_id"));
            
            System.out.println(checkAccess(commandLine.getOptionValue("user_name"), 
                    commandLine.getOptionValue("patient_id")));
        }            
    }    
}