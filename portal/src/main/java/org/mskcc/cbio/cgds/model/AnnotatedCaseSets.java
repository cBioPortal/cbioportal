package org.mskcc.cbio.cgds.model;

import java.util.*;

/**
 * Annotated Case Sets.
 *
 * @author Ethan Cerami
 */
public class AnnotatedCaseSets {
    private static final String ALL_COMPLETE_TUMORS = "ALL COMPLETE TUMORS";
    private static final String ALL_TUMORS = "ALL TUMORS";
    private static final String ALL = "ALL";

    private CaseList defaultCaseList;

    public AnnotatedCaseSets(List<CaseList> caseSetList, Integer priorityLevel) {
        this.defaultCaseList = determineDefaultCaseSet(caseSetList, priorityLevel);
    }

    public AnnotatedCaseSets(List<CaseList> caseSetList) {
        this(caseSetList, 0);
    }

    /**
     * Gets the "best" default case set.
     *
     * @return "best" default case set.
     */
    public CaseList getDefaultCaseList() {
        return defaultCaseList;
    }

    /**
     * This code makes an attempts at selecting the "best" default case set.
     *
     *
     * @param caseSetList List of all Case Sets.
     * @param priorityLevel Priority level, all priorities below this one will be ignored
     * @return the "best" default case set.
     */
    private CaseList determineDefaultCaseSet(List<CaseList> caseSetList, Integer priorityLevel) {
        List<CaseSetWithPriority> priCaseList = new ArrayList<CaseSetWithPriority>();
        for (CaseList caseSet : caseSetList) {
            Integer priority = null;

            // These are the new category overrides
            switch (caseSet.getCaseListCategory()) {
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
                registerPriorityCaseList(caseSet, ALL_COMPLETE_TUMORS, 3, priCaseList);
                registerPriorityCaseList(caseSet, ALL_TUMORS, 4, priCaseList);
                registerPriorityCaseList(caseSet, ALL, 5, priCaseList);
            } else {
                // If we define a higher t-hold, just shift the priority level
                if(priority < priorityLevel)
                    priority += 10;

                priCaseList.add(new CaseSetWithPriority(caseSet, priority));
            }
        }

        Collections.sort(priCaseList, new CaseSetWithPriorityComparator());
        if (priCaseList.size() > 0) {
            return priCaseList.get(0).getCaseList();
        } else {
            return failSafeReturn(caseSetList);
        }
    }

    private CaseList failSafeReturn(List<CaseList> caseSetList) {
        if (caseSetList.size() > 0) {
            return caseSetList.get(0);
        } else {
            return null;
        }
    }

    private void registerPriorityCaseList (CaseList caseSet, String target, int priority,
            List<CaseSetWithPriority> priCaseList) {
        String name = caseSet.getName();
        if (name.toUpperCase().startsWith(target)) {
            priCaseList.add(new CaseSetWithPriority(caseSet, priority));
        }
    }
}

class CaseSetWithPriority {
    private CaseList caseList;
    private int priority;

    CaseSetWithPriority(CaseList caseList, int priority) {
        this.caseList = caseList;
        this.priority = priority;
    }

    public CaseList getCaseList() {
        return caseList;
    }

    public int getPriority() {
        return priority;
    }
}

class CaseSetWithPriorityComparator implements Comparator {

    public int compare(Object o0, Object o1) {
        CaseSetWithPriority caseSet0 = (CaseSetWithPriority) o0;
        CaseSetWithPriority caseSet1 = (CaseSetWithPriority) o1;
        return new Integer(caseSet0.getPriority()).compareTo(caseSet1.getPriority());
    }
}