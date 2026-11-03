/*
 * Copyright (c) 2016 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.scripts;

import java.io.*;
import java.util.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.scripts.ImportCopyNumberSegmentData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;

/**
 * Test the import of Segment data into database.
 * @author pieterlukasse
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestImportCopyNumberSegmentData extends IntegrationTestBase {

	//To use in test cases where we expect an exception:
	@Rule
	public ExpectedException exception = ExpectedException.none();
    
	/**
     * Test importing of Clinical Data File.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
    public void testImportSegmentDataNewStudy() throws Exception {
		//new dummy study to simulate importing clinical data in empty study:
		CancerStudy cancerStudy = new CancerStudy("testnewseg","testnewseg","testnewseg","brca",true);
        cancerStudy.setReferenceGenome("hg19");
		DaoCancerStudy.addCancerStudy(cancerStudy);
        addTestPatientAndSampleRecords(new File("src/test/resources/segment/data_cna_hg19.seg"), cancerStudy);

        String[] args = {
        		"--data","src/test/resources/segment/data_cna_hg19.seg",
        		"--meta","src/test/resources/segment/meta_cna_hg19_seg.txt",
        		"--loadMode", "bulkLoad"
        		};
        ImportCopyNumberSegmentData runner = new ImportCopyNumberSegmentData(args);
        runner.run(); 
        //TODO : fix test to actually store data and add some checks 
       
	}

    private void addTestPatientAndSampleRecords(File file, CancerStudy cancerStudy) throws FileNotFoundException, IOException, DaoException {
        // extract sample ids from first column
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine(); // want to skip first line / header line
        List<String> sampleIds = new ArrayList<>();
        while ((line=buf.readLine()) != null) {
            String[] parts = line.split("\t");
            if (!sampleIds.contains(parts[0])) {
                sampleIds.add(parts[0]);
            }
        }
        reader.close();
        // add sample + patient records to db
        for (String sampleId : sampleIds) {
            // fetch patient from db or add new one if does not exist
            Patient p = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), sampleId);
            Integer pId = (p == null) ? DaoPatient.addPatient(new Patient(cancerStudy, sampleId)) : p.getInternalId();
            DaoSample.addSample(new Sample(sampleId, pId, cancerStudy.getTypeOfCancerId()));
        }
        ClickHouseBulkLoader.flushAll();
    }
}
