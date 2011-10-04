package org.mskcc.cgds.model;

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

    public AnnotatedCaseSets(List<CaseList> caseSetList) {
        this.defaultCaseList = determineDefaultCaseSet(caseSetList);
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
     * @param caseSetList List of all Case Sets.
     * @return the "best" default case set.
     */
    private CaseList determineDefaultCaseSet(List<CaseList> caseSetList) {
        List<CaseSetWithPriority> priCaseList = new ArrayList<CaseSetWithPriority>();
        for (CaseList caseSet : caseSetList) {
            registerPriorityCaseList(caseSet, ALL_COMPLETE_TUMORS, 0, priCaseList);
            registerPriorityCaseList(caseSet, ALL_TUMORS, 1, priCaseList);
            registerPriorityCaseList(caseSet, ALL, 2, priCaseList);
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