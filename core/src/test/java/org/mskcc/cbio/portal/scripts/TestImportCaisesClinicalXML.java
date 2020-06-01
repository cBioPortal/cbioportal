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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoTypeOfCancer;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.TypeOfCancer;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jgao
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestImportCaisesClinicalXML {
    
    @Before
    public void setUp() throws Exception {
        TypeOfCancer typeOfCancer = new TypeOfCancer();
        typeOfCancer.setTypeOfCancerId("prad");
        typeOfCancer.setName("prad");
        typeOfCancer.setShortName("prad");
        DaoTypeOfCancer.addTypeOfCancer(typeOfCancer);

        CancerStudy cancerStudy = new CancerStudy("prad","prad","prad","prad",true);
        cancerStudy.setReferenceGenome("hg19");
        DaoCancerStudy.addCancerStudy(cancerStudy);
        
        int studyId = DaoCancerStudy.getCancerStudyByStableId("prad").getInternalId();

        DaoPatient.addPatient(new Patient(cancerStudy, "97115001"));
        DaoPatient.addPatient(new Patient(cancerStudy, "97115002"));
        DaoPatient.addPatient(new Patient(cancerStudy, "97115003"));

        int patient1 = DaoPatient.getPatientByCancerStudyAndPatientId(studyId, "97115001").getInternalId();
        int patient2 = DaoPatient.getPatientByCancerStudyAndPatientId(studyId, "97115002").getInternalId();

        DaoSample.addSample(new Sample("SC_9022-Tumor", patient1, "prad"));
        DaoSample.addSample(new Sample("SC_9023-Tumor", patient2, "prad"));
    }
    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void test() throws Exception {
        File xmlFile = new File("target/test-classes/data_clinical_caises.xml");
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId("prad");
        ImportCaisesClinicalXML importCaisesClinicalXML = new ImportCaisesClinicalXML(null);
        importCaisesClinicalXML.setFile(xmlFile, cancerStudy);
        importCaisesClinicalXML.importData();
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
