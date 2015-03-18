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

package org.mskcc.cbio.portal.scripts;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoTypeOfCancer;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.TypeOfCancer;

/**
 *
 * @author jgao
 */
public class TestImportCaisesClinicalXML {
    
    public TestImportCaisesClinicalXML() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        try {
            ResetDatabase.resetDatabase();
            
            TypeOfCancer typeOfCancer = new TypeOfCancer();
            typeOfCancer.setTypeOfCancerId("prad");
            typeOfCancer.setName("prad");
            typeOfCancer.setShortName("prad");
            DaoTypeOfCancer.addTypeOfCancer(typeOfCancer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @AfterClass
    public static void tearDownClass() {
        
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void test() throws Exception {
        CancerStudy cancerStudy = new CancerStudy("prad","prad","prad","prad",true);
        cancerStudy.setInternalId(1);
        DaoCancerStudy.addCancerStudy(cancerStudy);

        DaoPatient.addPatient(new Patient(cancerStudy, "97115001"));
        DaoPatient.addPatient(new Patient(cancerStudy, "97115002"));
        DaoPatient.addPatient(new Patient(cancerStudy, "97115003"));

        int patient1 = DaoPatient.getPatientByCancerStudyAndPatientId(1, "97115001").getInternalId();
        int patient2 = DaoPatient.getPatientByCancerStudyAndPatientId(1, "97115002").getInternalId();
        //int patient3 = DaoPatient.getPatientByCancerStudyAndPatientId(1, "97115003").getInternalId();

        DaoSample.addSample(new Sample("SC_9022-Tumor", patient1, "prad"));
        DaoSample.addSample(new Sample("SC_9023-Tumor", patient2, "prad"));
        //DaoSample.addSample(new Sample("SC_9024-Tumor", patient3, "test"));
         
        File xmlFile = new File("target/test-classes/data_clinical_caises.xml");
        ImportCaisesClinicalXML.importData(xmlFile, 1);
     }
     
//     @Test
//     public void temp() throws Exception {
//        CancerStudy cancerStudy = new CancerStudy("prad_su2c","prad_su2c","prad_su2c","prad",true);
//        cancerStudy.setInternalId(1);
//        DaoCancerStudy.addCancerStudy(cancerStudy);
//        
//        ImportClinicalData.main(new String[]{"/Users/jgao/projects/cbio-portal-data/studies/prad/su2c/data_clinical.txt", "prad_su2c"});
//        ImportCaisesClinicalXML.main(new String[] {"--data","/Users/jgao/projects/cbio-portal-data/studies/prad/su2c/data_clinical_caises.xml",
//            "--meta","/Users/jgao/projects/cbio-portal-data/studies/prad/su2c/meta_clinical_caises.txt",
//            "--loadMode", "bulkLoad"});
//     }
}
