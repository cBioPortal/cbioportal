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

package org.mskcc.cbio.cgds.web_api;

import java.util.*;

import com.google.common.base.Joiner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mskcc.cbio.cgds.dao.DaoClinicalAttribute;
import org.mskcc.cbio.cgds.dao.DaoClinicalData;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.ClinicalAttribute;
import org.mskcc.cbio.cgds.model.ClinicalData;
import org.mskcc.cbio.cgds.model.Patient;

/**
 * Utility class to get clinical data
 */
public class GetClinicalData {
    private static final String NA = "NA";
    private static final String TAB = "\t";

    /**
     * Gets Clinical Data for the Specific Cases.
     *
     * @param caseIdList Target Case IDs.
     * @return String of Output.
     * @throws DaoException Database Error.
     */
    public static String getClinicalData(int cancerStudyId, Set<String> caseIdList, boolean includeFreeFormData)
            throws DaoException {

        List<Patient> caseSurvivalList = DaoClinicalData.getCases(cancerStudyId, caseIdList);
        Map<String,Patient> mapClinicalData = new HashMap<String,Patient>();
        for (Patient cd : caseSurvivalList) {
            mapClinicalData.put(cd.getCaseId(), cd);
        }

        Map<String,Map<String,String>> mapClinicalFreeForms = Collections.emptyMap();
        Set<String> freeFormParams = Collections.emptySet();
        if (includeFreeFormData) {
            List<ClinicalData> clinicalFreeForms = DaoClinicalData.getCasesByCases(cancerStudyId, new ArrayList(caseIdList));
            mapClinicalFreeForms = new HashMap<String,Map<String,String>>();
            freeFormParams = new HashSet<String>();
            for (ClinicalData cff : clinicalFreeForms) {
                freeFormParams.add(cff.getAttrId());
                String caseId = cff.getCaseId();
                Map<String,String> cffs = mapClinicalFreeForms.get(caseId);
                if (cffs==null) {
                    cffs = new HashMap<String,String>();
                    mapClinicalFreeForms.put(caseId, cffs);
                }
                cffs.put(cff.getAttrId(),cff.getAttrVal());
            }
        }

        StringBuilder buf = new StringBuilder();
        if (!caseSurvivalList.isEmpty() || !freeFormParams.isEmpty()) {
            buf.append("case_id");
            if (!caseSurvivalList.isEmpty()) {
                    buf.append("\toverall_survival_months\toverall_survival_status\t")
                       .append("disease_free_survival_months\tdisease_free_survival_status\tage_at_diagnosis");
            }
            for (String param : freeFormParams) {
                append(buf, param);
            }
            buf.append('\n');

            for (String caseId : caseIdList) {
                buf.append(caseId);
                if (!caseSurvivalList.isEmpty()) {
                    Patient cd = mapClinicalData.get(caseId);
                    append(buf, cd==null ? null : cd.getOverallSurvivalMonths());
                    append(buf, cd==null ? null : cd.getOverallSurvivalStatus());
                    append(buf, cd==null ? null : cd.getDiseaseFreeSurvivalMonths());
                    append(buf, cd==null ? null : cd.getDiseaseFreeSurvivalStatus());
                    append(buf, cd==null ? null : cd.getAgeAtDiagnosis());
                }

                Map<String,String> cff = mapClinicalFreeForms.get(caseId);
                for (String param : freeFormParams) {
                    append(buf, cff==null ? null : cff.get(param));
                }

                buf.append('\n');
            }
            return buf.toString();
        } else {
            buf.append("Error:  No clinical data available for the case set or "
                    + "case lists specified.  Number of cases:  ")
                    .append(caseIdList.size()).append("\n");
            return buf.toString();
        }
    }

    private static void append(StringBuilder buf, Object o) {
        buf.append(TAB).append(o==null ? NA : o);
    }

    /**
     * takes an object (Clinical or ClinicalAttribute) and
     * converts it to a map (JSONObject)
     *
     * @param clinical
     * @return
     */
    public static JSONObject reflectToMap(ClinicalData clinical) {
        JSONObject map = new JSONObject();

        map.put("attr_id", clinical.getAttrId());
        map.put("attr_val", clinical.getAttrVal());
        //TODO: at some point we may want to incorporate the cancer_study_id
//        map.put("cancer_study_id", Integer.toString(clinical.getCancerStudyId()));
        map.put("sample", clinical.getCaseId());

        return map;
    }

    public static JSONObject reflectToMap(ClinicalAttribute clinicalAttribute) {
        JSONObject map = new JSONObject();

        map.put("attr_id", clinicalAttribute.getAttrId());
        map.put("datatype", clinicalAttribute.getDatatype());
        map.put("description", clinicalAttribute.getDescription());
        map.put("display_name", clinicalAttribute.getDisplayName());

        return map;
    }

    public static JSONArray clinicals2JSONArray(List<ClinicalData> clinicals) {
        JSONArray toReturn = new JSONArray();
        for (ClinicalData c : clinicals) {
            toReturn.add(reflectToMap(c));
        }
        return toReturn;
    }

    /**
     * Returns a single row the database
     *
     * @param cancerStudyId
     * @param caseId
     * @param attrId
     */
    public static JSONObject getJsonDatum(String cancerStudyId, String caseId, String attrId) throws DaoException {
        return reflectToMap(DaoClinicalData.getDatum(cancerStudyId, caseId, attrId));
    }

    public static String getTxtDatum(String cancerStudyId, String caseId, String attrId) throws DaoException {
        ClinicalData c = DaoClinicalData.getDatum(cancerStudyId, caseId, attrId);

        return "" + c.getCaseId() + "\t" + c.getAttrId() + "\t" + c.getAttrVal();
    }

    /**
     * Creates a json object with data and attributes fields that correspond to the data
     * in the clinicals and the set of attributes that exist in the clinicals
     * @param clinicals
     * @return
     * @throws DaoException
     */
    public static JSONObject generateJson(List<ClinicalData> clinicals) throws DaoException {
        Set<JSONObject> attrs = new HashSet<JSONObject>();
        JSONObject toReturn = new JSONObject();
        JSONArray data = new JSONArray();

        for (ClinicalData c : clinicals) {
//            if (!c.getAttrVal().equalsIgnoreCase(NA)) { // filter out NAs
            data.add(reflectToMap(c));
            ClinicalAttribute attr = DaoClinicalAttribute.getDatum(c.getAttrId());
            attrs.add(reflectToMap(attr));
//            }
        }

        Iterator<JSONObject> attrsIt = attrs.iterator();
        JSONArray attributes = new JSONArray();
        while (attrsIt.hasNext()) {
            attributes.add(attrsIt.next());
        }

        toReturn.put("data", data);
        toReturn.put("attributes", attributes);

        return toReturn;
    }
    /**
     *
     * @param cancerStudyId
     * @return An object with 2 fields:
     * -- data: array of object literals corresponding to rows in the database
     * -- attributes: array of clinical attribute metadatas (object literals) that appear in the data
     * @throws DaoException
     */
    public static JSONObject getJSON(String cancerStudyId, List<String> caseIds) throws DaoException {
        List<ClinicalData> clinicals = DaoClinicalData.getData(cancerStudyId, caseIds);

        return generateJson(clinicals);
    }

    public static JSONObject getJSON(String cancerStudyId, List<String> caseIds, String attrId) throws DaoException {

        ClinicalAttribute attr = DaoClinicalAttribute.getDatum(attrId);
        List<ClinicalData> clinicals = DaoClinicalData.getData(cancerStudyId, caseIds, attr);

        return generateJson(clinicals);
    }

    /**
     * Takes a list of clinicals and turns them into a tab-delimited, new-line ended string.
     *
     * invariants : 1. they all must have the same caseId
     *              2. no repeats
     *
     * @param clinicals
     * @return
     */
    public static String makeRow(List<ClinicalData> clinicals) {
        // TODO: this needs to be sorted

        String row = clinicals.get(0).getCaseId();


        for (ClinicalData c : clinicals) {
            row = row + "\t" + c.getAttrVal();
        }

        return row + "\n";
    }

    public static String getTxt(String cancerStudyId, List<String> caseIds) throws DaoException {
        List<ClinicalData> allClinicals = DaoClinicalData.getData(cancerStudyId, caseIds);

        HashMap<String, List<ClinicalData>> caseId2Clinical = new HashMap<String, List<ClinicalData>>();
        for (ClinicalData c : allClinicals) {
            List<ClinicalData> got = caseId2Clinical.get(c.getCaseId());

            if (got == null) {
                got = new ArrayList<ClinicalData>();
                got.add(c);
                caseId2Clinical.put(c.getCaseId(), got);
            } else {
                got.add(c);
            }
        }

        // make header, is order preserved across all rows?
        List<ClinicalData> aClinical = caseId2Clinical.values().iterator().next();
        List<String> headers = new ArrayList<String>();
        for (ClinicalData c : aClinical) {
            headers.add(c.getAttrId().toLowerCase());
        }

        String txt = "case_id\t" + Joiner.on("\t").join(headers) + "\n";      // start out with just a header

        for (List<ClinicalData> clinicals : caseId2Clinical.values()) {
            txt += makeRow(clinicals);
        }

        return txt;
    }
}
