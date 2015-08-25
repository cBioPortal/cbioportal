/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit Tests for the Annotated Patient Sets.
 *
 * @author Ethan Cerami.
 */
public class TestAnnotatedPatientSets {
	
	List<PatientList> patientSetList;
	
	@Before 
	public void setUp() {
		patientSetList = new ArrayList<PatientList>();
        PatientList patientList0 = new PatientList("all", 1, 2, "all gbm", PatientListCategory.OTHER);
        PatientList patientList1 = new PatientList("all", 1, 2, "all tumors", PatientListCategory.OTHER);
        PatientList patientList2 = new PatientList("all", 1, 2, "expression subset 1", PatientListCategory.OTHER);
        patientSetList.add(patientList0);
        patientSetList.add(patientList1);
        patientSetList.add(patientList2);
    }
	
	@Test
    public void test1() {
        AnnotatedPatientSets annotatedPatientSets = new AnnotatedPatientSets(patientSetList);
        PatientList defaultPatientSet = annotatedPatientSets.getDefaultPatientList();
        assertEquals("all tumors", defaultPatientSet.getName());
    }
    @Test
    public void test2() {
        PatientList patientList3 = new PatientList("all", 1, 2, "all complete tumors", PatientListCategory.OTHER);
        patientSetList.add(patientList3);
        AnnotatedPatientSets annotatedPatientSets = new AnnotatedPatientSets(patientSetList);
        PatientList defaultPatientSet = annotatedPatientSets.getDefaultPatientList();
        assertEquals("all complete tumors", defaultPatientSet.getName());
    }
    @Test
    public void test3() {
        patientSetList = new ArrayList<PatientList>();
        AnnotatedPatientSets annotatedPatientSets = new AnnotatedPatientSets(patientSetList);
        PatientList defaultPatientSet = annotatedPatientSets.getDefaultPatientList();
        assertEquals(null, defaultPatientSet);
    }
    @Test
    public void test4() {
        patientSetList = new ArrayList<PatientList>();
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
    
}