package org.mskcc.cbio.cgds.model;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JUnit Tests for the Annotated Case Sets.
 *
 * @author Ethan Cerami.
 */
public class TestAnnotatedCaseSets extends TestCase {

    public void test1() {
        List<CaseList> caseSetList = initCaseList();
        AnnotatedCaseSets annotatedCaseSets = new AnnotatedCaseSets(caseSetList);
        CaseList defaultCaseSet = annotatedCaseSets.getDefaultCaseList();
        assertEquals("all tumors", defaultCaseSet.getName());
    }

    public void test2() {
        List<CaseList> caseSetList = initCaseList();
        CaseList caseList3 = new CaseList("all", 1, 2, "all complete tumors", CaseListCategory.OTHER);
        caseSetList.add(caseList3);
        AnnotatedCaseSets annotatedCaseSets = new AnnotatedCaseSets(caseSetList);
        CaseList defaultCaseSet = annotatedCaseSets.getDefaultCaseList();
        assertEquals("all complete tumors", defaultCaseSet.getName());
    }

    public void test3() {
        List<CaseList> caseSetList = new ArrayList<CaseList>();
        AnnotatedCaseSets annotatedCaseSets = new AnnotatedCaseSets(caseSetList);
        CaseList defaultCaseSet = annotatedCaseSets.getDefaultCaseList();
        assertEquals(null, defaultCaseSet);
    }

    public void test4() {
        List<CaseList> caseSetList = new ArrayList<CaseList>();
        CaseList caseList0 = new CaseList("exp1", 1, 2, "exp1", CaseListCategory.OTHER);
        CaseList caseList1 = new CaseList("exp2", 1, 2, "exp2", CaseListCategory.OTHER);
        CaseList caseList2 = new CaseList("exp3", 1, 2, "exp3", CaseListCategory.OTHER);
        caseSetList.add(caseList0);
        caseSetList.add(caseList1);
        caseSetList.add(caseList2);
        AnnotatedCaseSets annotatedCaseSets = new AnnotatedCaseSets(caseSetList);
        CaseList defaultCaseSet = annotatedCaseSets.getDefaultCaseList();
        assertEquals("exp1", defaultCaseSet.getName());
    }

    private List<CaseList> initCaseList() {
        List<CaseList> caseSetList = new ArrayList<CaseList>();
        CaseList caseList0 = new CaseList("all", 1, 2, "all gbm", CaseListCategory.OTHER);
        CaseList caseList1 = new CaseList("all", 1, 2, "all tumors", CaseListCategory.OTHER);
        CaseList caseList2 = new CaseList("all", 1, 2, "expression subset 1", CaseListCategory.OTHER);
        caseSetList.add(caseList0);
        caseSetList.add(caseList1);
        caseSetList.add(caseList2);
        return caseSetList;
    }
}