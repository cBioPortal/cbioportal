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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mskcc.cbio.cgds.dao.DaoClinicalData;
import org.mskcc.cbio.cgds.dao.DaoClinicalFreeForm;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.ClinicalData;
import org.mskcc.cbio.cgds.model.ClinicalFreeForm;

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
    public static String getClinicalData(Set<String> caseIdList, boolean includeFreeFormData)
            throws DaoException {
        DaoClinicalData daoClinical = new DaoClinicalData();
        DaoClinicalFreeForm daoClinicalFreeForm = new DaoClinicalFreeForm();
        
        List<ClinicalData> caseSurvivalList = daoClinical.getCases(caseIdList);
        Map<String,ClinicalData> mapClinicalData = new HashMap<String,ClinicalData>();
        for (ClinicalData cd : caseSurvivalList) {
            mapClinicalData.put(cd.getCaseId(), cd);
        }
        
        Map<String,Map<String,String>> mapClinicalFreeForms = Collections.emptyMap();
        Set<String> freeFormParams = Collections.emptySet();
        if (includeFreeFormData) {
            List<ClinicalFreeForm> clinicalFreeForms = daoClinicalFreeForm.getCasesByCases(caseIdList);
            mapClinicalFreeForms = new HashMap<String,Map<String,String>>();
            freeFormParams = new HashSet<String>();
            for (ClinicalFreeForm cff : clinicalFreeForms) {
                freeFormParams.add(cff.getParamName());
                String caseId = cff.getCaseId();
                Map<String,String> cffs = mapClinicalFreeForms.get(caseId);
                if (cffs==null) {
                    cffs = new HashMap<String,String>();
                    mapClinicalFreeForms.put(caseId, cffs);
                }
                cffs.put(cff.getParamName(),cff.getParamValue());
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
                    ClinicalData cd = mapClinicalData.get(caseId);
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
}
