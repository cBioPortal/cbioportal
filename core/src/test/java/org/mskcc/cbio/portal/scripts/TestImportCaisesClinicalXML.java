/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
            typeOfCancer.setTypeOfCancerId("test");
            typeOfCancer.setName("test");
            typeOfCancer.setShortName("test");
            DaoTypeOfCancer.addTypeOfCancer(typeOfCancer);

            CancerStudy cancerStudy = new CancerStudy("test","test","test","test",true);
            cancerStudy.setInternalId(1);
            DaoCancerStudy.addCancerStudy(cancerStudy);
            
            DaoPatient.addPatient(new Patient(cancerStudy, "97115001"));
            DaoPatient.addPatient(new Patient(cancerStudy, "97115002"));
            DaoPatient.addPatient(new Patient(cancerStudy, "97115003"));
            
            int patient1 = DaoPatient.getPatientByCancerStudyAndPatientId(1, "97115001").getInternalId();
            int patient2 = DaoPatient.getPatientByCancerStudyAndPatientId(1, "97115002").getInternalId();
            //int patient3 = DaoPatient.getPatientByCancerStudyAndPatientId(1, "97115003").getInternalId();
            
            DaoSample.addSample(new Sample("SC_9022-Tumor", patient1, "test"));
            DaoSample.addSample(new Sample("SC_9023-Tumor", patient2, "test"));
            //DaoSample.addSample(new Sample("SC_9024-Tumor", patient3, "test"));
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
        File xmlFile = new File("target/test-classes/data_clinical_caises.xml");
        ImportCaisesClinicalXML.importData(xmlFile, 1);
     }
}
