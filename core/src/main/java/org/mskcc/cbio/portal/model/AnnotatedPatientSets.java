/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.model;

import java.util.*;

/**
 * Annotated Patient Sets.
 *
 * @author Ethan Cerami
 */
public class AnnotatedPatientSets {
    private static final String ALL_COMPLETE_TUMORS = "ALL COMPLETE TUMORS";
    private static final String ALL_TUMORS = "ALL TUMORS";
    private static final String ALL = "ALL";

    private PatientList defaultPatientList;

    public AnnotatedPatientSets(List<PatientList> patientSetList, Integer priorityLevel) {
        this.defaultPatientList = determineDefaultPatientSet(patientSetList, priorityLevel);
    }

    public AnnotatedPatientSets(List<PatientList> patientSetList) {
        this(patientSetList, 0);
    }

    /**
     * Gets the "best" default patient set.
     *
     * @return "best" default patient set.
     */
    public PatientList getDefaultPatientList() {
        return defaultPatientList;
    }

    /**
     * This code makes an attempts at selecting the "best" default patient set.
     *
     *
     * @param patientSetList List of all Patient Sets.
     * @param priorityLevel Priority level, all priorities below this one will be ignored
     * @return the "best" default patient set.
     */
    private PatientList determineDefaultPatientSet(List<PatientList> patientSetList, Integer priorityLevel) {
        List<PatientSetWithPriority> priPatientList = new ArrayList<PatientSetWithPriority>();
        for (PatientList patientSet : patientSetList) {
            Integer priority = null;

            // These are the new category overrides
            switch (patientSet.getPatientListCategory()) {
                case ALL_CASES_WITH_MUTATION_AND_CNA_DATA:
                    priority = 0;
                    break;
                case ALL_CASES_WITH_MUTATION_DATA:
                    priority = 1;
                    break;
                case ALL_CASES_WITH_CNA_DATA:
                    priority = 2;
                    break;
            }

            // If category matches none of the overrides, fallback to the old way
            if(priority == null) {
                registerPriorityPatientList(patientSet, ALL_COMPLETE_TUMORS, 3, priPatientList);
                registerPriorityPatientList(patientSet, ALL_TUMORS, 4, priPatientList);
                registerPriorityPatientList(patientSet, ALL, 5, priPatientList);
            } else {
                // If we define a higher t-hold, just shift the priority level
                if(priority < priorityLevel)
                    priority += 10;

                priPatientList.add(new PatientSetWithPriority(patientSet, priority));
            }
        }

        Collections.sort(priPatientList, new PatientSetWithPriorityComparator());
        if (priPatientList.size() > 0) {
            return priPatientList.get(0).getPatientList();
        } else {
            return failSafeReturn(patientSetList);
        }
    }

    private PatientList failSafeReturn(List<PatientList> patientSetList) {
        if (patientSetList.size() > 0) {
            return patientSetList.get(0);
        } else {
            return null;
        }
    }

    private void registerPriorityPatientList (PatientList patientSet, String target, int priority,
            List<PatientSetWithPriority> priPatientList) {
        String name = patientSet.getName();
        if (name.toUpperCase().startsWith(target)) {
            priPatientList.add(new PatientSetWithPriority(patientSet, priority));
        }
    }
}

class PatientSetWithPriority {
    private PatientList patientList;
    private int priority;

    PatientSetWithPriority(PatientList patientList, int priority) {
        this.patientList = patientList;
        this.priority = priority;
    }

    public PatientList getPatientList() {
        return patientList;
    }

    public int getPriority() {
        return priority;
    }
}

class PatientSetWithPriorityComparator implements Comparator {

    public int compare(Object o0, Object o1) {
        PatientSetWithPriority patientSet0 = (PatientSetWithPriority) o0;
        PatientSetWithPriority patientSet1 = (PatientSetWithPriority) o1;
        return new Integer(patientSet0.getPriority()).compareTo(patientSet1.getPriority());
    }
}