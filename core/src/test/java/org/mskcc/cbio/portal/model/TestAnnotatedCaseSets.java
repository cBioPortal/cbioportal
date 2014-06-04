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

package org.mskcc.cbio.portal.model;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.model.*;

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