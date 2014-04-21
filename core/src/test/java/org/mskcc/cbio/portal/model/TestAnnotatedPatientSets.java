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
 * JUnit Tests for the Annotated Patient Sets.
 *
 * @author Ethan Cerami.
 */
public class TestAnnotatedPatientSets extends TestCase {

    public void test1() {
        List<PatientList> patientSetList = initPatientList();
        AnnotatedPatientSets annotatedPatientSets = new AnnotatedPatientSets(patientSetList);
        PatientList defaultPatientSet = annotatedPatientSets.getDefaultPatientList();
        assertEquals("all tumors", defaultPatientSet.getName());
    }

    public void test2() {
        List<PatientList> patientSetList = initPatientList();
        PatientList patientList3 = new PatientList("all", 1, 2, "all complete tumors", PatientListCategory.OTHER);
        patientSetList.add(patientList3);
        AnnotatedPatientSets annotatedPatientSets = new AnnotatedPatientSets(patientSetList);
        PatientList defaultPatientSet = annotatedPatientSets.getDefaultPatientList();
        assertEquals("all complete tumors", defaultPatientSet.getName());
    }

    public void test3() {
        List<PatientList> patientSetList = new ArrayList<PatientList>();
        AnnotatedPatientSets annotatedPatientSets = new AnnotatedPatientSets(patientSetList);
        PatientList defaultPatientSet = annotatedPatientSets.getDefaultPatientList();
        assertEquals(null, defaultPatientSet);
    }

    public void test4() {
        List<PatientList> patientSetList = new ArrayList<PatientList>();
        PatientList patientList0 = new PatientList("exp1", 1, 2, "exp1", PatientListCategory.OTHER);
        PatientList patientList1 = new PatientList("exp2", 1, 2, "exp2", PatientListCategory.OTHER);
        PatientList patientList2 = new PatientList("exp3", 1, 2, "exp3", PatientListCategory.OTHER);
        patientSetList.add(patientList0);
        patientSetList.add(patientList1);
        patientSetList.add(patientList2);
        AnnotatedPatientSets annotatedPatientSets = new AnnotatedPatientSets(patientSetList);
        PatientList defaultPatientSet = annotatedPatientSets.getDefaultPatientList();
        assertEquals("exp1", defaultPatientSet.getName());
    }

    private List<PatientList> initPatientList() {
        List<PatientList> patientSetList = new ArrayList<PatientList>();
        PatientList patientList0 = new PatientList("all", 1, 2, "all gbm", PatientListCategory.OTHER);
        PatientList patientList1 = new PatientList("all", 1, 2, "all tumors", PatientListCategory.OTHER);
        PatientList patientList2 = new PatientList("all", 1, 2, "expression subset 1", PatientListCategory.OTHER);
        patientSetList.add(patientList0);
        patientSetList.add(patientList1);
        patientSetList.add(patientList2);
        return patientSetList;
    }
}