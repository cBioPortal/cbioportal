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

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.web_api.*;

import org.json.simple.*;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * This Servlet Returns a JSON Representation of all Cancer Studies and all
 * Gene Sets within the Portal.
 *
 * @author Ethan Cerami.
 */
public class PortalMetaDataJSON extends HttpServlet {

	// class which process access control to cancer studies
	private AccessControl accessControl;

    /**
     * Initializes the servlet.
     *
     * @throws ServletException Serlvet Init Error.
     */
    public void init() throws ServletException {
        super.init();
		ApplicationContext context = 
			new ClassPathXmlApplicationContext("classpath:applicationContext-security.xml");
		accessControl = (AccessControl)context.getBean("accessControl");
	}

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servelt Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException            IO Error.
     */
    protected void doGet(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        XDebug xdebug = new XDebug(httpServletRequest);

        try {
            Map rootMap = new LinkedHashMap();
            Map cancerStudyMap = new LinkedHashMap();

            // Also get cancer study types and add it to the metadata
            ArrayList<TypeOfCancer> allTypesOfCancer = DaoTypeOfCancer.getAllTypesOfCancer();
            Collections.sort(allTypesOfCancer, new Comparator<TypeOfCancer>() {
                @Override
                public int compare(TypeOfCancer typeOfCancer, TypeOfCancer typeOfCancer1) {
                    return typeOfCancer.getName().compareTo(typeOfCancer1.getName());
                }
            });
            Map<String, String> typeOfCancerMap = new HashMap<String, String>();
            Map<String, String> visibleTypeOfCancerMap = new HashMap<String, String>();
            Map<String, String> cancerColors = new HashMap<String, String>();
            Map<String, String> visibleCancerColors = new HashMap<String, String>();
            Map<String, String> shortNames = new HashMap<String, String>();
            Map<String, String> visibleShortNames = new HashMap<String, String>();
            for (TypeOfCancer typeOfCancer : allTypesOfCancer) {
                typeOfCancerMap.put(typeOfCancer.getTypeOfCancerId(), typeOfCancer.getName());
                cancerColors.put(typeOfCancer.getTypeOfCancerId(), typeOfCancer.getDedicatedColor());
                shortNames.put(typeOfCancer.getTypeOfCancerId(), typeOfCancer.getShortName());
            }

            //  Cancer All Cancer Studies
            List<CancerStudy> cancerStudiesList = accessControl.getCancerStudies();

            //  Get all Genomic Profiles and Patient Sets for each Cancer Study
            rootMap.put("cancer_studies", cancerStudyMap);
            for (CancerStudy cancerStudy : cancerStudiesList) {
                ArrayList<PatientList> patientSets = GetPatientLists.getPatientLists(cancerStudy.getCancerStudyStableId());

                ArrayList<GeneticProfile> geneticProfiles =
                        GetGeneticProfiles.getGeneticProfiles(cancerStudy.getCancerStudyStableId());

                JSONArray jsonGenomicProfileList = new JSONArray();
                for (GeneticProfile geneticProfile : geneticProfiles) {
                    Map map = new LinkedHashMap();
                    map.put("id", geneticProfile.getStableId());
                    map.put("alteration_type", geneticProfile.getGeneticAlterationType().toString());
                    map.put("show_in_analysis_tab", geneticProfile.showProfileInAnalysisTab());
                    map.put("name", geneticProfile.getProfileName());
                    map.put("description", geneticProfile.getProfileDescription());
					map.put("datatype", geneticProfile.getDatatype());
                    jsonGenomicProfileList.add(map);
                }

                JSONArray jsonPatientList = new JSONArray();
                for (PatientList patientSet : patientSets) {
                    Map map = new LinkedHashMap();
                    map.put("id", patientSet.getStableId());
                    map.put("name", patientSet.getName());
                    map.put("description", patientSet.getDescription());
                    map.put("size", patientSet.getPatientList().size());
                    jsonPatientList.add(map);
                }
                Map jsonCancerStudySubMap = new LinkedHashMap();
                jsonCancerStudySubMap.put("name", cancerStudy.getName());
                jsonCancerStudySubMap.put("short_name", cancerStudy.getShortName());
                jsonCancerStudySubMap.put("description", cancerStudy.getDescription());
                jsonCancerStudySubMap.put("citation", cancerStudy.getCitation());
                jsonCancerStudySubMap.put("pmid", cancerStudy.getPmid());
                jsonCancerStudySubMap.put("genomic_profiles", jsonGenomicProfileList);
                jsonCancerStudySubMap.put("case_sets", jsonPatientList);
                jsonCancerStudySubMap.put("has_mutation_data", cancerStudy.hasMutationData(geneticProfiles));
                jsonCancerStudySubMap.put("has_cna_data", cancerStudy.hasCnaData());
                jsonCancerStudySubMap.put("has_mutsig_data", cancerStudy.hasMutSigData());
                jsonCancerStudySubMap.put("has_gistic_data", cancerStudy.hasGisticData());
                cancerStudyMap.put(cancerStudy.getCancerStudyStableId(), jsonCancerStudySubMap);

                String typeOfCancerId = cancerStudy.getTypeOfCancerId();
                jsonCancerStudySubMap.put("type_of_cancer", typeOfCancerId);
                visibleTypeOfCancerMap.put(typeOfCancerId, typeOfCancerMap.get(typeOfCancerId));
                visibleCancerColors.put(typeOfCancerId, cancerColors.get(typeOfCancerId));
                visibleShortNames.put(typeOfCancerId, shortNames.get(typeOfCancerId));
            }

            // Only put visible ones
            rootMap.put("type_of_cancers", visibleTypeOfCancerMap);
            rootMap.put("cancer_colors", visibleCancerColors);
            rootMap.put("short_names", visibleShortNames);

            //  Get all Gene Sets
            GeneSetUtil geneSetUtil = GeneSetUtil.getInstance();
            Map jsonGeneSetMap = new LinkedHashMap();
            rootMap.put("gene_sets", jsonGeneSetMap);
            ArrayList<GeneSet> geneSetList = geneSetUtil.getGeneSetList();
            for (GeneSet geneSet : geneSetList) {
                Map geneSetMap = new LinkedHashMap();
                geneSetMap.put("name", geneSet.getName());
                geneSetMap.put("gene_list", geneSet.getGeneList());
                jsonGeneSetMap.put(geneSet.getId(), geneSetMap);
            }

            httpServletResponse.setContentType("application/json");
            String jsonText = JSONValue.toJSONString(rootMap);
            PrintWriter writer = httpServletResponse.getWriter();
            writer.write(jsonText);
            writer.flush();
            writer.close();
        } catch (DaoException e) {
            throw new ServletException(e);
        } catch (ProtocolException e) {
            throw new ServletException(e);
		}
    }

}