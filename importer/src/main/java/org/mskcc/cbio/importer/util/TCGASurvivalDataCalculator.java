/** Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.importer.util;

import org.mskcc.cbio.importer.model.DataMatrix;
import org.mskcc.cbio.importer.SurvivalDataCalculator;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.importer.model.SurvivalStatus;

import java.util.*;
import java.util.regex.*;

public class TCGASurvivalDataCalculator implements SurvivalDataCalculator
{
    private static final float AVG_NUM_DAYS_MONTH = 30.44f;

    private static final String PATIENT_ID = "bcr_patient_barcode";
    private static final String FOLLOW_UP_PATIENT_ID = "bcr_followup_barcode";

    private static final String VITAL_STATUS = "vital_status";
    private static final String DAYS_TO_DEATH = "death_days_to";//"days_to_death";
    private static final String LAST_FOLLOW_UP = "last_contact_days_to";//"days_to_last_followup";
    private static final String LAST_KNOWN_ALIVE = "days_to_last_known_alive";
    private static final String NEW_TUMOR_EVENT = "new_tumor_event_dx_days_to";//"days_to_new_tumor_event_after_initial_treatment";
    private static final Pattern FOLLOW_UP_PATIENT_ID_REGEX = Pattern.compile("^(TCGA-\\w\\w-\\w\\w\\w\\w)-.*$");
    
    private List<String> canonicalPatientList;

    private static enum MissingAttributeValues
    {
        NOT_APPLICABLE("Not Applicable"),
        NOT_AVAILABLE("Not Available"),
        PENDING("Pending"),
        DISCREPANCY("Discrepancy"),
        COMPLETED("Completed"),
        NULL("null"),
        MISSING("");

        private String propertyName;
        
        MissingAttributeValues(String propertyName) { this.propertyName = propertyName; }
        public String toString() { return propertyName; }

        static public boolean has(String value) {
            if (value == null) return false;
            if (value.equals("")) return true;
            try { 
                value = value.replaceAll("[\\[|\\]]", "");
                value = value.replaceAll(" ", "_");
                return valueOf(value.toUpperCase()) != null; 
            }
            catch (IllegalArgumentException x) { 
                return false;
            }
        }

        static public String getNotAvailable()
        {
            return "[" + NOT_AVAILABLE.toString() + "]";
        }
    }

    private static enum VitalStatusAlive
    {
        ALIVE("Alive"),
        LIVING("LIVING");

        private String propertyName;
        
        VitalStatusAlive(String propertyName) { this.propertyName = propertyName; }
        public String toString() { return propertyName; }

        static public boolean has(String value) {
            if (value == null) return false;
            try { 
                return valueOf(value.toUpperCase()) != null; 
            }
            catch (IllegalArgumentException x) { 
                return false;
            }
        }
    }

    private static enum VitalStatusDead
    {
        DEAD("Dead"),
        DECEASED("DECEASED");

        private String propertyName;
        
        VitalStatusDead(String propertyName) { this.propertyName = propertyName; }
        public String toString() { return propertyName; }

        static public boolean has(String value) {
            if (value == null) return false;
            try { 
                return valueOf(value.toUpperCase()) != null; 
            }
            catch (IllegalArgumentException x) { 
                return false;
            }
        }
    }

    private static enum DiseaseFreeStatus
    {
        DISEASE_FREE("DiseaseFree"),
        DISEASED("Recurred/Progressed");

        private String propertyName;
        
        DiseaseFreeStatus(String propertyName) { this.propertyName = propertyName; }
        public String toString() { return propertyName; }
    }

    private class ClinicalDataRecord
    {
        String lastFollowUp;
        String lastKnownAlive;
        String vitalStatus;
    }

    private class SurvivalData extends ClinicalDataRecord
    {
        String daysToDeath;
    }

    private class DiseaseFreeData extends ClinicalDataRecord
    {
        String daysToNewTumorEventAfterInitialTreatment;
    }

    public TCGASurvivalDataCalculator()
    {
        this.canonicalPatientList = new ArrayList<String>();
    }

    /**
     * The list is in ascending (time) order,
     * i.e., patient matrix would come before follow-up matrices.
     */
    @Override
    public SurvivalStatus computeSurvivalData(List<DataMatrix> dataMatrices)
    {
        canonicalPatientList = getPatientIds(dataMatrices.get(0));
        SurvivalStatus oss = new SurvivalStatus();
        computeSurvivalData(oss, getSurvivalData(dataMatrices));
        computeDiseaseFreeData(oss, getDiseaseFreeData(dataMatrices));
        return oss;
    }

    private Map<String, SurvivalData> getSurvivalData(List<DataMatrix> dataMatrices)
    {
        Map<String, SurvivalData> survivalData = new HashMap<String, SurvivalData>();

        for (DataMatrix dataMatrix : dataMatrices) {
            mergeSurvivalData(getSurvivalDataForMatrix(dataMatrix), survivalData);
        }

        return survivalData;
    }

    private Map<String,SurvivalData> getSurvivalDataForMatrix(DataMatrix dataMatrix)
    {
        Map<String, SurvivalData> survivalDataMap = new HashMap<String, SurvivalData>();

        List<String> patientIds = getPatientIds(dataMatrix);
        for (int lc = 0; lc < patientIds.size(); lc++) {
            String patientId = patientIds.get(lc);
            SurvivalData sd = getSurvivalDataForPatient(lc, dataMatrix);
            if (survivalDataMap.containsKey(patientId)) {
                sd = mergeSurvivalData(survivalDataMap.get(patientId), sd);
            }
            survivalDataMap.put(patientId, sd);
        }

        return survivalDataMap;
    }

    private List<String> getPatientIds(DataMatrix dataMatrix)
    {
        List<LinkedList<String>> patientIds = dataMatrix.getColumnData(PATIENT_ID);
        if (patientIds.isEmpty()) {
            patientIds = dataMatrix.getColumnData(FOLLOW_UP_PATIENT_ID);
        }

        return (patientIds.isEmpty()) ?
            Collections.<String>emptyList() :
            convertFollowupPatientIds(patientIds.get(0));
    }

    private List<String> convertFollowupPatientIds(List<String> patientIds)
    {
        List<String> convertedPatientIds = new ArrayList<String>();

        for (String patientId : patientIds) {
            Matcher patientIdMatcher = FOLLOW_UP_PATIENT_ID_REGEX.matcher(patientId);
            convertedPatientIds.add(patientIdMatcher.find() ? patientIdMatcher.group(1) : patientId);
        }

        return convertedPatientIds;
    }

    private SurvivalData getSurvivalDataForPatient(int patientIndex, DataMatrix dataMatrix)
    {
        SurvivalData survivalData = new SurvivalData();

        survivalData.vitalStatus = getValue(patientIndex, VITAL_STATUS, dataMatrix);
        survivalData.daysToDeath = getValue(patientIndex, DAYS_TO_DEATH, dataMatrix);
        survivalData.lastFollowUp = getValue(patientIndex, LAST_FOLLOW_UP, dataMatrix);
        survivalData.lastKnownAlive = getValue(patientIndex, LAST_KNOWN_ALIVE, dataMatrix);
        
        return survivalData;
    }

    private String getValue(int index, String columnName, DataMatrix dataMatrix)
    {
        List<LinkedList<String>> columnData = dataMatrix.getColumnData(columnName);

        String value = MissingAttributeValues.getNotAvailable();
        if (!columnData.isEmpty()) {
            value = columnData.get(0).get(index);
        }
        return (value.isEmpty()) ? MissingAttributeValues.getNotAvailable() : value;
    }

    private SurvivalData mergeSurvivalData(SurvivalData sd1, SurvivalData sd2)
    {
        SurvivalData mergedSurvivalData = new SurvivalData();

        mergedSurvivalData.lastFollowUp = mergeWorksheetValues(sd1.lastFollowUp, sd2.lastFollowUp);
        mergedSurvivalData.lastKnownAlive = mergeWorksheetValues(sd1.lastKnownAlive, sd2.lastKnownAlive);
        mergedSurvivalData.vitalStatus = mergeWorksheetValues(sd1.vitalStatus, sd2.vitalStatus);
        mergedSurvivalData.daysToDeath = mergeWorksheetValues(sd1.daysToDeath, sd2.daysToDeath);

        return mergedSurvivalData;
    }

    private String mergeWorksheetValues(String value1, String value2)
    {
        if (atLeastOneInteger(value1, value2)) {
            return returnGreaterInteger(value1, value2);
        }
        else {
            return returnHigherPrecendenceString(value1, value2);
        }
    }

    private boolean atLeastOneInteger(String value1, String value2)
    {
        boolean value1IsInt = valueIsInteger(value1);
        boolean value2IsInt = valueIsInteger(value2);

        return (value1IsInt || value2IsInt);
    }

    private boolean valueIsInteger(String value)
    {
        try {
            Integer.parseInt(value);
            return true;
        }
        catch (NumberFormatException e) {}
        return false;
    }

    // routine assumes either value1 or value2 or both are integers.
    private String returnGreaterInteger(String value1, String value2)
    {
        boolean value1IsInt = valueIsInteger(value1);
        boolean value2IsInt = valueIsInteger(value2);

        try {
            if (value1IsInt && value2IsInt) {
                return (Integer.parseInt(value1) > Integer.parseInt(value2)) ? value1 : value2;
            }
            else if (value1IsInt) {
                return value1;
            }
            else {
                return value2;
            }
        }
        catch (NumberFormatException e) {}

        assert true == false : String.format("returnGreaterInteger, %s, %s", value1, value2);
        return "-1";
    }

    private String returnHigherPrecendenceString(String value1, String value2)
    {
        if (value1.equals(value2)) {
            return value1;
        }
        else if (oneStringEmpty(value1, value2)) {
            return returnNonEmptyString(value1, value2);
        }
        else if (vitalStatusChanged(value1, value2)) {
            return VitalStatusDead.DEAD.toString();
        }
        else if (missingAttributeChanged(value1, value2)) {
            return returnHigherPrecedenceMissingAttribute(value1, value2);
        }

        assert true == false : String.format("returnHigherPrecendenceString, %s, %s", value1, value2);
        return MissingAttributeValues.MISSING.toString();
    }

    private boolean oneStringEmpty(String value1, String value2)
    {
        return (value1.length() == 0 || value2.length() == 0);
    }

    // routine assumes either value1 or value2 are empty, but not both
    private String returnNonEmptyString(String value1, String value2)
    {
        return (value1.length() > 0) ? value1 : value2;
    }

    private boolean vitalStatusChanged(String value1, String value2)
    {
        return ((VitalStatusAlive.has(value1) ||
                 VitalStatusDead.has(value1) ||
                 VitalStatusAlive.has(value2) ||
                 VitalStatusDead.has(value2)) &&
                !value1.equals(value2));
    }

    private boolean missingAttributeChanged(String value1, String value2)
    {
        return (!value1.equals(value2) &&
                MissingAttributeValues.has(value1) &&
                MissingAttributeValues.has(value2));
    }
    
    // assumes value1 and value2 are Missing attributes and not equal
    private String returnHigherPrecedenceMissingAttribute(String value1, String value2)
    {
        if (value1.equals(MissingAttributeValues.NULL.toString()) &&
            MissingAttributeValues.has(value2) &&
            !value2.equals(MissingAttributeValues.NULL.toString())) {
            return value1;
        }
        else if (value2.equals(MissingAttributeValues.NULL.toString()) &&
                 MissingAttributeValues.has(value1) &&
                 !value1.equals(MissingAttributeValues.NULL.toString())) {
            return value2;
        }

        assert true == false : String.format("returnHigherPrecendenceMissingAttribute, %s, %s", value1, value2);
        return MissingAttributeValues.MISSING.toString();
    }

    private void mergeSurvivalData(Map<String,SurvivalData> sourceMap, Map<String, SurvivalData> destMap)
    {
        for (String patientId : sourceMap.keySet()) {
            SurvivalData survivalData = sourceMap.get(patientId);
            if (destMap.containsKey(patientId)) {
                destMap.put(patientId, mergeSurvivalData(destMap.get(patientId), survivalData));
            }
            else {
                destMap.put(patientId, survivalData);
            }
        }
    }

    private void computeSurvivalData(SurvivalStatus oss, Map<String, SurvivalData> survivalData)
    {
        oss.osStatus = computeOverallSurvivalStatus(survivalData);
        oss.osMonths = computeOverallSurvivalMonths(survivalData);
    }

    private List<String> computeOverallSurvivalStatus(Map<String, SurvivalData> survivalData)
    {
        List<String> osStatus = initializeList(canonicalPatientList.size());
        for (String patientId : survivalData.keySet()) {
            int osStatusIndex = canonicalPatientList.indexOf(patientId);
            SurvivalData sd = survivalData.get(patientId);
            if (VitalStatusAlive.has(sd.vitalStatus)) {
                osStatus.set(osStatusIndex, VitalStatusAlive.LIVING.toString());
            }
            else if (VitalStatusDead.has(sd.vitalStatus)) {
                osStatus.set(osStatusIndex, VitalStatusDead.DECEASED.toString());
            }
            else {
                osStatus.set(osStatusIndex, ClinicalAttribute.NA);
            }
        }
        return osStatus;
    }

    private List<String> computeOverallSurvivalMonths(Map<String, SurvivalData> survivalData)
    {
        List<String> osStatusMonths = initializeList(canonicalPatientList.size());
        for (String patientId : survivalData.keySet()) {
            int osStatusMonthsIndex = canonicalPatientList.indexOf(patientId);
            SurvivalData sd = survivalData.get(patientId);
            if (VitalStatusAlive.has(sd.vitalStatus)) {
                osStatusMonths.set(osStatusMonthsIndex, convertDaysToMonths(sd.lastFollowUp, sd.lastKnownAlive));
            }
            else if (VitalStatusDead.has(sd.vitalStatus)) {
                osStatusMonths.set(osStatusMonthsIndex, convertDaysToMonths(sd.daysToDeath));
            }
            else {
                osStatusMonths.set(osStatusMonthsIndex, ClinicalAttribute.NA);
            }
        }

        return osStatusMonths;
    }

    private String convertDaysToMonths(String... sdValues)
    {
        if (sdValues.length == 1)
        {
            return (MissingAttributeValues.has(sdValues[0])) ? 
                    ClinicalAttribute.MISSING :
                    convertDaysToMonthsAsString(Integer.parseInt(sdValues[0]));
        }
        else if (sdValues.length == 2)
        {
            boolean sdValue0Missing = MissingAttributeValues.has(sdValues[0]);
            boolean sdValue1Missing = MissingAttributeValues.has(sdValues[1]);
            if (sdValue0Missing && sdValue1Missing) {
                return ClinicalAttribute.NA;
            }
            else if (!sdValue0Missing && sdValue1Missing) {
                return convertDaysToMonthsAsString(Integer.parseInt(sdValues[0]));
            }
            else if (sdValue0Missing && !sdValue1Missing) {
                return convertDaysToMonthsAsString(Integer.parseInt(sdValues[1]));
            }
            else {
                return convertDaysToMonthsAsString(Math.max(Integer.parseInt(sdValues[0]),
                                                            Integer.parseInt(sdValues[1])));
            }
        }

        return ClinicalAttribute.NA;
    }

    private String convertDaysToMonthsAsString(int days)
    {
        return String.format("%.2f", days / AVG_NUM_DAYS_MONTH);
    }

    private Map<String, DiseaseFreeData> getDiseaseFreeData(List<DataMatrix> dataMatrices)
    {
        Map<String, DiseaseFreeData> diseaseFreeData = new HashMap<String, DiseaseFreeData>();

        for (DataMatrix dataMatrix : dataMatrices) {
            mergeDiseaseFreeData(getDiseaseFreeDataForMatrix(dataMatrix), diseaseFreeData);
        }

        return diseaseFreeData;
    }

    private Map<String,DiseaseFreeData> getDiseaseFreeDataForMatrix(DataMatrix dataMatrix)
    {
        Map<String, DiseaseFreeData> diseaseFreeDataMap = new HashMap<String, DiseaseFreeData>();

        List<String> patientIds = getPatientIds(dataMatrix);
        for (int lc = 0; lc < patientIds.size(); lc++) {
            String patientId = patientIds.get(lc);
            DiseaseFreeData df = getDiseaseFreeDataForPatient(lc, dataMatrix);
            if (diseaseFreeDataMap.containsKey(patientId)) {
                df = mergeDiseaseFreeData(diseaseFreeDataMap.get(patientId), df);
            }
            diseaseFreeDataMap.put(patientId, df);
        }

        return diseaseFreeDataMap;
    }

    private DiseaseFreeData getDiseaseFreeDataForPatient(int patientIndex, DataMatrix dataMatrix)
    {
        DiseaseFreeData diseaseFreeData = new DiseaseFreeData();

        diseaseFreeData.vitalStatus = getValue(patientIndex, VITAL_STATUS, dataMatrix);
        diseaseFreeData.lastFollowUp = getValue(patientIndex, LAST_FOLLOW_UP, dataMatrix);
        diseaseFreeData.lastKnownAlive = getValue(patientIndex, LAST_KNOWN_ALIVE, dataMatrix);
        diseaseFreeData.daysToNewTumorEventAfterInitialTreatment = getValue(patientIndex, NEW_TUMOR_EVENT, dataMatrix);

        return diseaseFreeData;
    }

    private DiseaseFreeData mergeDiseaseFreeData(DiseaseFreeData df1, DiseaseFreeData df2)
    {
        DiseaseFreeData mergedDiseaseFreeData = new DiseaseFreeData();

        mergedDiseaseFreeData.lastFollowUp = mergeWorksheetValues(df1.lastFollowUp, df2.lastFollowUp);
        mergedDiseaseFreeData.lastKnownAlive = mergeWorksheetValues(df1.lastKnownAlive, df2.lastKnownAlive);
        mergedDiseaseFreeData.vitalStatus = mergeWorksheetValues(df1.vitalStatus, df2.vitalStatus);
        mergedDiseaseFreeData.daysToNewTumorEventAfterInitialTreatment =
            mergeWorksheetValues(df1.daysToNewTumorEventAfterInitialTreatment, df2.daysToNewTumorEventAfterInitialTreatment);

        return mergedDiseaseFreeData;
    }
    
    private void mergeDiseaseFreeData(Map<String,DiseaseFreeData> sourceMap, Map<String, DiseaseFreeData> destMap)
    {
        for (String patientId : sourceMap.keySet()) {
            DiseaseFreeData diseaseFreeData = sourceMap.get(patientId);
            if (destMap.containsKey(patientId)) {
                destMap.put(patientId, mergeDiseaseFreeData(destMap.get(patientId), diseaseFreeData));
            }
            else {
                destMap.put(patientId, diseaseFreeData);
            }
        }
    }

    private void computeDiseaseFreeData(SurvivalStatus oss, Map<String, DiseaseFreeData> diseaseFreeData)
    {
        oss.dfStatus = computeDiseaseFreeStatus(diseaseFreeData);
        oss.dfMonths = computeDiseaseFreeMonths(diseaseFreeData);
    }

    private List<String> computeDiseaseFreeStatus(Map<String, DiseaseFreeData> diseaseFreeData)
    {
        List<String> dfStatus = initializeList(canonicalPatientList.size());
        for (String patientId : diseaseFreeData.keySet()) {
            int dfStatusIndex = canonicalPatientList.indexOf(patientId);
            DiseaseFreeData df = diseaseFreeData.get(patientId);
            if (patientIsDiseaseFree(df)) {
                dfStatus.set(dfStatusIndex, DiseaseFreeStatus.DISEASE_FREE.toString());
            }
            else {
                try {
                    Integer.parseInt(df.daysToNewTumorEventAfterInitialTreatment);
                    dfStatus.set(dfStatusIndex, DiseaseFreeStatus.DISEASED.toString());
                }
                catch(NumberFormatException e) {
                    dfStatus.set(dfStatusIndex, ClinicalAttribute.NA);
                }
            }
        }
        return dfStatus;
    }

    private List<String> computeDiseaseFreeMonths(Map<String, DiseaseFreeData> diseaseFreeData)
    {
        List<String> dfStatusMonths = initializeList(canonicalPatientList.size());
        for (String patientId : diseaseFreeData.keySet()) {
            int dfStatusMonthsIndex = canonicalPatientList.indexOf(patientId);
            DiseaseFreeData df = diseaseFreeData.get(patientId);
            try {
                if (patientIsDiseaseFree(df)) {
                    dfStatusMonths.set(dfStatusMonthsIndex, convertDaysToMonths(df.lastFollowUp, df.lastKnownAlive));
                }
                else {
                    int dfStatusDays = Integer.parseInt(df.daysToNewTumorEventAfterInitialTreatment);
                    dfStatusMonths.set(dfStatusMonthsIndex, convertDaysToMonths(Integer.toString(dfStatusDays)));
                }
            }
            catch(NumberFormatException e) {
                dfStatusMonths.set(dfStatusMonthsIndex, ClinicalAttribute.NA);
            }
        }

        return dfStatusMonths;
    }

    private boolean patientIsDiseaseFree(DiseaseFreeData df)
    {
        return (VitalStatusAlive.has(df.vitalStatus) &&
                (df.daysToNewTumorEventAfterInitialTreatment.equals(MissingAttributeValues.NULL.toString()) ||
                 df.daysToNewTumorEventAfterInitialTreatment.equals(MissingAttributeValues.getNotAvailable())));
    }

    private List<String> initializeList(int size)
    {
        List<String> os = new ArrayList<String>(size);
        for (int lc = 0; lc < size; lc++) {
            os.add(lc, "");
        }
        return os;
    }

    private void dumpSurvivalData(Map<String, List<SurvivalData>> survivalData)
    {
        List<String> daysToDeath = new ArrayList<String>();
        List<String> lastFollowUp = new ArrayList<String>();
        List<String> lastKnownAlive = new ArrayList<String>();
        List<String> vitalStatus = new ArrayList<String>();

        for (String patientId : new TreeSet<String>(survivalData.keySet())) {
            for (SurvivalData sd : survivalData.get(patientId)) {
                daysToDeath.add(sd.daysToDeath);
                lastFollowUp.add(sd.lastFollowUp);
                lastKnownAlive.add(sd.lastKnownAlive);
                vitalStatus.add(sd.vitalStatus);
            }

            System.out.print(patientId + "\t\t");
            printList(daysToDeath);
            printList(lastFollowUp);
            printList(lastKnownAlive);
            printList(vitalStatus);
            System.out.println();

            daysToDeath.clear();
            lastFollowUp.clear();
            lastKnownAlive.clear();
            vitalStatus.clear();
        }
    }

    private void printList(List<String> list)
    {
        System.out.print("(");
        int ct = 0;
        for (String value : list) {
            if (value.isEmpty()) {
                value = "[]";
            }
            System.out.print(value);
            if (++ct < list.size()) {
                System.out.print(", ");
            }
        }
        System.out.print(")\t\t");
    }
}
