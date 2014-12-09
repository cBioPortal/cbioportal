/**
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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
package org.mskcc.cbio.portal.servlet;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.web_api.*;

import org.json.simple.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * This Servlet Returns a JSON Representation of all Cancer Studies and all Gene
 * Sets within the Portal.
 *
 * @author Ethan Cerami.
 */
public class PortalMetaDataJSON extends HttpServlet {

    // class which process access control to cancer studies
    private AccessControl accessControl;
    public static final String STUDY_ID = "study_id";
    public static final String PARTIAL_STUDIES = "partial_studies";
    public static final String GENESET_ID = "geneset_id";
    public static final String PARTIAL_GENESETS = "partial_genesets";

    /**
     * Initializes the servlet.
     *
     * @throws ServletException Serlvet Init Error.
     */

    public void init() throws ServletException {
        super.init();
        accessControl = SpringUtil.getAccessControl();
    }

    /**
     * Generates a Map which will be turned into JSON representing a cancer
     * study
     *
     * @param cancerStudy A CancerStudy object to be turned into a map
     * @param partial Whether the generated Map should be "partial", i.e.
     * including only the name and type of cancer of the study, or not,
     * including all information about a cancer study.
     * @return A Map representing the given study.
     * @throws DaoException
     */
    protected Map cancerStudyMap(CancerStudy cancerStudy, boolean partial) throws DaoException {
        Map ret = new LinkedHashMap();
        ret.put("name", cancerStudy.getName());
        ret.put("type_of_cancer", cancerStudy.getTypeOfCancerId());
        ret.put("description", cancerStudy.getDescription());

        if (partial) {
            ret.put("partial", "true");
        } else {
            // at this point we have the study corresponding to the given ID
            ArrayList<PatientList> caseSets = GetPatientLists.getPatientLists(cancerStudy.getCancerStudyStableId());

            ArrayList<GeneticProfile> geneticProfiles
                    = GetGeneticProfiles.getGeneticProfiles(cancerStudy.getCancerStudyStableId());

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

            JSONArray jsonCaseList = new JSONArray();
            for (PatientList caseSet : caseSets) {
                Map map = new LinkedHashMap();
                map.put("id", caseSet.getStableId());
                map.put("name", caseSet.getName());
                map.put("description", caseSet.getDescription());
                map.put("size", caseSet.getPatientList().size());
                jsonCaseList.add(map);
            }
            ret.put("short_name", cancerStudy.getShortName());
            ret.put("citation", cancerStudy.getCitation());
            ret.put("pmid", cancerStudy.getPmid());
            ret.put("genomic_profiles", jsonGenomicProfileList);
            ret.put("case_sets", jsonCaseList);
            ret.put("has_mutation_data", cancerStudy.hasMutationData(geneticProfiles));
            ret.put("has_cna_data", cancerStudy.hasCnaData());
            ret.put("has_mutsig_data", cancerStudy.hasMutSigData());
            ret.put("has_gistic_data", cancerStudy.hasGisticData());

            ret.put("partial", "false");
        }

        return ret;
    }

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest Http Servlet Request Object.
     * @param httpServletResponse Http Servelt Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException IO Error.
     */
    protected void doGet(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        XDebug xdebug = new XDebug(httpServletRequest);
        String studyId = httpServletRequest.getParameter(STUDY_ID);
        String geneSetId = httpServletRequest.getParameter(GENESET_ID);

        try {
            if (geneSetId != null) {
                String geneList = "";
                GeneSetUtil geneSetUtil = GeneSetUtil.getInstance();
                ArrayList<GeneSet> geneSetList = geneSetUtil.getGeneSetList();
                for (GeneSet geneSet : geneSetList) {
                    if (geneSet.getId().replace("/", "").equals(geneSetId)) {
                        geneList = geneSet.getGeneList();
                        break;
                    }
                }
                Map obj = new LinkedHashMap();
                obj.put("list", geneList);
                String jsonText = JSONValue.toJSONString(obj);
                PrintWriter writer = httpServletResponse.getWriter();
                writer.write(jsonText);
                writer.flush();
                writer.close();
                return;
            }
            if (studyId != null) {
                // check if it's a valid study ID first
                // If not, return {}
                CancerStudy cancerStudy = null;
                List<CancerStudy> cancerStudiesList = accessControl.getCancerStudies();
                for (CancerStudy study : cancerStudiesList) {
                    if (study.getCancerStudyStableId().equals(studyId)) {
                        cancerStudy = study;
                        break;
                    }
                }
                if (cancerStudy == null) {
                    String jsonText = JSONValue.toJSONString(new LinkedHashMap());
                    PrintWriter writer = httpServletResponse.getWriter();
                    writer.write(jsonText);
                    writer.flush();
                    writer.close();
                    return;
                }
                // at this point we have a CancerStudy object corresponding to
                //  the given study
                Map jsonCancerStudySubMap = cancerStudyMap(cancerStudy, false);
                httpServletResponse.setContentType("application/json");
                String jsonText = JSONValue.toJSONString(jsonCancerStudySubMap);
                PrintWriter writer = httpServletResponse.getWriter();
                writer.write(jsonText);
                writer.flush();
                writer.close();
            } else {
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

                //  Get all Genomic Profiles and Case Sets for each Cancer Study
                rootMap.put("cancer_studies", cancerStudyMap);
                String partial_studies_s = httpServletRequest.getParameter(PARTIAL_STUDIES);
                
                boolean full_studies_data = (partial_studies_s == null || partial_studies_s.equals("false"));
                
                for (CancerStudy cancerStudy : cancerStudiesList) {
                    Map jsonCancerStudySubMap = cancerStudyMap(cancerStudy, !full_studies_data);
                    cancerStudyMap.put(cancerStudy.getCancerStudyStableId(), jsonCancerStudySubMap);
                    String typeOfCancerId = cancerStudy.getTypeOfCancerId();
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
                String partial_genesets_s = httpServletRequest.getParameter(PARTIAL_GENESETS);
                boolean full_genesets_data = (partial_genesets_s == null || partial_genesets_s.equals("false"));
                
                for (GeneSet geneSet : geneSetList) {
                    Map geneSetMap = new LinkedHashMap();
                    geneSetMap.put("name", geneSet.getName());
                    if (full_genesets_data) {
                        geneSetMap.put("gene_list", geneSet.getGeneList());
                    } else {
                        geneSetMap.put("gene_list", "");
                    }
                    jsonGeneSetMap.put(geneSet.getId(), geneSetMap);
                }

                httpServletResponse.setContentType("application/json");
                String jsonText = JSONValue.toJSONString(rootMap);
                PrintWriter writer = httpServletResponse.getWriter();
                writer.write(jsonText);
                writer.flush();
                writer.close();
            }
        } catch (DaoException e) {
            throw new ServletException(e);
        } catch (ProtocolException e) {
            throw new ServletException(e);
        }
    }

}
