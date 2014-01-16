/** Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.importer.util;

import org.mskcc.cbio.importer.model.DataMatrix;
import org.mskcc.cbio.importer.SurvivalDataCalculator;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.importer.model.SurvivalStatus;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class TCGASurvivalDataCalculator implements SurvivalDataCalculator
{
    private static final float AVG_NUM_DAYS_MONTH = 30.44f;

    private static final String PATIENT_ID = "bcr_patient_barcode";
    private static final String FOLLOW_UP_PATIENT_ID = "bcr_followup_barcode";

    private static final String VITAL_STATUS = "vital_status";
    private static final String DAYS_TO_DEATH = "days_to_death";
    private static final String LAST_FOLLOW_UP = "days_to_last_followup";
    private static final String LAST_KNOWN_ALIVE = "days_to_last_known_alive";
    private static final String NEW_TUMOR_EVENT = "days_to_new_tumor_event_after_initial_treatment";

    private static final Pattern FOLLOW_UP_PATIENT_ID_REGEX = Pattern.compile("^(TCGA-\\w\\w-\\w\\w\\w\\w)-.*$");

    private static enum MissingAttributeValues
    {
        NOT_APPLICABLE("Not Applicable"),
        NOT_AVAILABLE("Not Available"),
        NULL("null"),
        MISSING("");

        private String propertyName;
        
        MissingAttributeValues(String propertyName) { this.propertyName = propertyName; }
        public String toString() { return propertyName; }

        static public boolean has(String value) {
            if (value == null) return false;
            try { 
                value = value.replaceAll("[\\[|\\]]", "");
                value = value.replaceAll(" ", "_");
                return valueOf(value.toUpperCase()) != null; 
            }
            catch (IllegalArgumentException x) { 
                return false;
            }
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

    private class SurvivalData
    {
        String vitalStatus;
        String daysToDeath;
        String lastFollowUp;
        String lastKnownAlive;
    }

    private class DiseaseFreeData
    {
        String daysToNewTumorEventAfterInitialTreatment;
    }

    /**
     * The list is in ascending (time) order,
     * i.e., patient matrix would come before follow-up matrices.
     */
    @Override
    public SurvivalStatus computeSurvivalData(List<DataMatrix> dataMatrices)
    {
        SurvivalStatus oss = new SurvivalStatus();
        computeSurvivalData(oss, dataMatrices, getSurvivalData(dataMatrices));
        computeDiseaseFreeData(oss, dataMatrices, getDiseaseFreeData(dataMatrices));
        return oss;
    }

    private Map<String, List<SurvivalData>> getSurvivalData(List<DataMatrix> dataMatrices)
    {
        Map<String, List<SurvivalData>> survivalData = new HashMap<String, List<SurvivalData>>();

        for (DataMatrix dataMatrix : dataMatrices) {
            mergeSurvivalData(getSurvivalDataForMatrix(dataMatrix), survivalData);
        }

        return survivalData;
    }

    private Map<String,SurvivalData> getSurvivalDataForMatrix(DataMatrix dataMatrix)
    {
        Map<String, SurvivalData> survivalDataMap = new HashMap<String, SurvivalData>();

        List<String> patientIds = getPatientIds(dataMatrix);
        for (String patientId : patientIds) {
            survivalDataMap.put(patientId, getSurvivalDataForPatient(patientIds.indexOf(patientId), dataMatrix));
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
        return (columnData.isEmpty()) ? "" : columnData.get(0).get(index);
    }

    private void mergeSurvivalData(Map<String,SurvivalData> sourceMap, Map<String, List<SurvivalData>> destMap)
    {
        for (String patientId : sourceMap.keySet()) {
            SurvivalData survivalData = sourceMap.get(patientId);
            if (destMap.containsKey(patientId)) {
                destMap.get(patientId).add(survivalData);
            }
            else {
                List<SurvivalData> survivalDataList = new ArrayList<SurvivalData>();
                survivalDataList.add(survivalData);
                destMap.put(patientId, survivalDataList);
            }
        }
    }

    private void computeSurvivalData(SurvivalStatus oss, List<DataMatrix> dataMatrices, Map<String, List<SurvivalData>> survivalData)
    {
        oss.osStatus = computeOverallSurvivalStatus(survivalData);
        oss.osMonths = computeOverallSurvivalMonths(survivalData);
    }

    private List<String> computeOverallSurvivalStatus(Map<String, List<SurvivalData>> survivalData)
    {
        List<String> osStatus = initializeList(survivalData.keySet().size());
        int osStatusIndex = -1;
        for (String patientId : new TreeSet<String>(survivalData.keySet())) {
            ++osStatusIndex;
            for (SurvivalData sd : survivalData.get(patientId)) {
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
        }
        return osStatus;
    }

    private List<String> computeOverallSurvivalMonths(Map<String, List<SurvivalData>> survivalData)
    {
        List<String> osStatusMonths = initializeList(survivalData.keySet().size());
        int osStatusMonthsIndex = -1;
        for (String patientId : new TreeSet<String>(survivalData.keySet())) {
            ++osStatusMonthsIndex;
            for (SurvivalData sd : survivalData.get(patientId)) {
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

    private Map<String, List<DiseaseFreeData>> getDiseaseFreeData(List<DataMatrix> dataMatrices)
    {
        Map<String, List<DiseaseFreeData>> diseaseFreeData = new HashMap<String, List<DiseaseFreeData>>();

        for (DataMatrix dataMatrix : dataMatrices) {
            mergeDiseaseFreeData(getDiseaseFreeDataForMatrix(dataMatrix), diseaseFreeData);
        }

        return diseaseFreeData;
    }

    private Map<String,DiseaseFreeData> getDiseaseFreeDataForMatrix(DataMatrix dataMatrix)
    {
        Map<String, DiseaseFreeData> diseaseFreeDataMap = new HashMap<String, DiseaseFreeData>();

        List<String> patientIds = getPatientIds(dataMatrix);
        for (String patientId : patientIds) {
            diseaseFreeDataMap.put(patientId, getDiseaseFreeDataForPatient(patientIds.indexOf(patientId), dataMatrix));
        }

        return diseaseFreeDataMap;
    }

    private DiseaseFreeData getDiseaseFreeDataForPatient(int patientIndex, DataMatrix dataMatrix)
    {
        DiseaseFreeData diseaseFreeData = new DiseaseFreeData();

        diseaseFreeData.daysToNewTumorEventAfterInitialTreatment = getValue(patientIndex, NEW_TUMOR_EVENT, dataMatrix);
        
        return diseaseFreeData;
    }

    private void mergeDiseaseFreeData(Map<String,DiseaseFreeData> sourceMap, Map<String, List<DiseaseFreeData>> destMap)
    {
        for (String patientId : sourceMap.keySet()) {
            DiseaseFreeData diseaseFreeData = sourceMap.get(patientId);
            if (destMap.containsKey(patientId)) {
                destMap.get(patientId).add(diseaseFreeData);
            }
            else {
                List<DiseaseFreeData> diseaseFreeDataList = new ArrayList<DiseaseFreeData>();
                diseaseFreeDataList.add(diseaseFreeData);
                destMap.put(patientId, diseaseFreeDataList);
            }
        }
    }

    private void computeDiseaseFreeData(SurvivalStatus oss, List<DataMatrix> dataMatrices, Map<String, List<DiseaseFreeData>> diseaseFreeData)
    {
        oss.dfStatus = computeDiseaseFreeStatus(diseaseFreeData);
        oss.dfMonths = computeDiseaseFreeMonths(diseaseFreeData);
    }

    private List<String> computeDiseaseFreeStatus(Map<String, List<DiseaseFreeData>> diseaseFreeData)
    {
        List<String> dfStatus = initializeList(diseaseFreeData.keySet().size());
        int dfStatusIndex = -1;
        for (String patientId : new TreeSet<String>(diseaseFreeData.keySet())) {
            ++dfStatusIndex;
            for (DiseaseFreeData df : diseaseFreeData.get(patientId)) {
                if (df.daysToNewTumorEventAfterInitialTreatment.equals(MissingAttributeValues.NULL.toString())) {
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
        }
        return dfStatus;
    }

    private List<String> computeDiseaseFreeMonths(Map<String, List<DiseaseFreeData>> diseaseFreeData)
    {
        List<String> dfStatusMonths = initializeList(diseaseFreeData.keySet().size());
        int dfStatusMonthsIndex = -1;
        for (String patientId : new TreeSet<String>(diseaseFreeData.keySet())) {
            ++dfStatusMonthsIndex;
            for (DiseaseFreeData df : diseaseFreeData.get(patientId)) {
                if (df.daysToNewTumorEventAfterInitialTreatment.equals(MissingAttributeValues.NULL.toString())) {
                    dfStatusMonths.set(dfStatusMonthsIndex, ClinicalAttribute.NA);
                }
                else {
                    try {
                        int dfStatusDays = Integer.parseInt(df.daysToNewTumorEventAfterInitialTreatment);
                        dfStatusMonths.set(dfStatusMonthsIndex, convertDaysToMonths(Integer.toString(dfStatusDays)));
                    }
                    catch(NumberFormatException e) {
                        dfStatusMonths.set(dfStatusMonthsIndex, ClinicalAttribute.NA);
                    }
                }
            }
        }

        return dfStatusMonths;
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
