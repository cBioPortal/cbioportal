package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.model.PatientList;

import java.util.Formatter;
import java.util.List;

public class OncoPrintUtil {

    /**
     * Constructs the OncoPrint patient set description.
     *
     * @param patientSetId String
     * @param patientSets List<PatientList>
     *
     * @return String
     */
    public static String getPatientSetDescription(String patientSetId, List<PatientList> patientSets) {

        StringBuilder builder = new StringBuilder();
        for (PatientList patientSet : patientSets) {
            if (patientSetId.equals(patientSet.getStableId())) {
                builder.append(patientSet.getName() + ": " + patientSet.getDescription());
            }
        }
        return builder.toString();
    }

    /**
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
}
