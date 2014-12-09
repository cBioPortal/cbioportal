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

package org.mskcc.cbio.portal.web_api;

import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mskcc.cbio.portal.dao.DaoClinicalAttribute;
import org.mskcc.cbio.portal.dao.DaoClinicalData;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.model.ClinicalData;
import org.mskcc.cbio.portal.model.Patient;

/**
 * Utility class to get clinical data
 */
public class GetClinicalData {
    private static final String NA = "NA";
    private static final String TAB = "\t";

    /**
     * Gets clinical data for specified cases.
     *
     * @param setOfCaseIds Case IDs.
     * @return an ArrayList of Survival Objects
     * @throws DaoException, as of August 2011 GetClinicalData has direct access to DAO Objects.
     */
    public static List<Patient> getClinicalData(int cancerStudyId, HashSet<String> setOfCaseIds) throws DaoException {
        if (setOfCaseIds != null && setOfCaseIds.size() > 0) {
            return DaoClinicalData.getSurvivalData(cancerStudyId, setOfCaseIds);
        } else {
            return Collections.emptyList();
        }
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
        map.put("sample", clinical.getStableId());

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
     * @param sampleId
     * @param attrId
     */
    public static JSONObject getJsonDatum(int cancerStudyId, String sampleId, String attrId) throws DaoException {
        ClinicalAttribute attr = DaoClinicalAttribute.getDatum(attrId);
        List<ClinicalData> data = DaoClinicalData.getSampleAndPatientData(cancerStudyId, Collections.singletonList(sampleId), attr);
        if (data.isEmpty()) {
            return new JSONObject();
        }
        return reflectToMap(data.get(0));
    }

    public static String getTxtDatum(int cancerStudyId, String sampleId, String attrId) throws DaoException {
        ClinicalAttribute attr = DaoClinicalAttribute.getDatum(attrId);
        List<ClinicalData> data = DaoClinicalData.getSampleAndPatientData(cancerStudyId, Collections.singletonList(sampleId), attr);
        if (data.isEmpty()) {
            return "";
        }

        ClinicalData c = data.get(0);
        return "" + c.getStableId() + "\t" + c.getAttrId() + "\t" + c.getAttrVal();
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

        Set<String> attrIds = new HashSet<String>();
        for (ClinicalData c : clinicals) {
//            if (!c.getAttrVal().equalsIgnoreCase(NA)) { // filter out NAs
            data.add(reflectToMap(c));
            attrIds.add(c.getAttrId());
//            }
        }
        
        for (ClinicalAttribute attr : DaoClinicalAttribute.getDatum(attrIds)) {
            attrs.add(reflectToMap(attr));
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
    public static JSONObject getJSON(int cancerStudyId, List<String> sampleIds) throws DaoException {
        List<ClinicalData> clinicals = DaoClinicalData.getSampleAndPatientData(cancerStudyId, sampleIds);

        return generateJson(clinicals);
    }

    public static JSONObject getJSON(int cancerStudyId, List<String> sampleIds, String attrId) throws DaoException {

        ClinicalAttribute attr = DaoClinicalAttribute.getDatum(attrId);
        List<ClinicalData> clinicals = DaoClinicalData.getSampleAndPatientData(cancerStudyId, sampleIds, attr);

        return generateJson(clinicals);
    }

    
    

    private static final Map<String, Integer> clinicalAttributeRank = new HashMap<String, Integer>();
    static {
        clinicalAttributeRank.put(null, -1);
        clinicalAttributeRank.put("CASE_ID", -1); // EXCLUDE
        clinicalAttributeRank.put("PATIENT_ID", 1);
    }
    private static int getClinicalAttributeRank(String attrId) {
        Integer r = clinicalAttributeRank.get(attrId);
        if (r==null) {
            return 1000;
        }
        return r;
    }
    
    /**
     * Takes a list of clinicals and turns them into a tab-delimited, new-line ended string.
     *
     * invariants : 1. they all must have the same sampleId
     *              2. no repeats
     *
     * @param clinicals
     * @return
     */
    public static String getTxt(int cancerStudyId, List<String> sampleIds) throws DaoException {
        List<ClinicalData> allClinicals = DaoClinicalData.getSampleAndPatientData(cancerStudyId, sampleIds);
        
        TreeSet<String> headers = new TreeSet<String>(new Comparator<String>() {
                @Override
                public int compare(String str1, String str2) {
                    Integer r1 = getClinicalAttributeRank(str1);
                    Integer r2 = getClinicalAttributeRank(str2);
                    if (r1.equals(r2)) {
                        return str1.compareTo(str2);
                    }
                    
                    return r1.compareTo(r2);
                }
        });
        Map<String, Map<String,ClinicalData>> sampleId2Clinical = new HashMap<String, Map<String,ClinicalData>>();
        for (ClinicalData c : allClinicals) {
            if (getClinicalAttributeRank(c.getAttrId())<0) {
                continue;
            }
            
            Map<String,ClinicalData> got = sampleId2Clinical.get(c.getStableId());

            if (got == null) {
                got = new HashMap<String,ClinicalData>();
                sampleId2Clinical.put(c.getStableId(), got);
            }
            
            got.put(c.getAttrId(),c);
            headers.add(c.getAttrId());
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("CASE_ID");
        for (String h : headers) {
            sb.append('\t').append(h);
        }
        sb.append('\n');

        for (Map.Entry<String, Map<String,ClinicalData>> entry : sampleId2Clinical.entrySet()) {
            String sampleId = entry.getKey();
            sb.append(sampleId);
            Map<String,ClinicalData> value = entry.getValue();
            for (String h : headers) {
                sb.append('\t');
                ClinicalData cd = value.get(h);
                if (cd!=null) {
                    sb.append(cd.getAttrVal());
                }
            }
            sb.append('\n');
        }

        return sb.toString();
    }
}
