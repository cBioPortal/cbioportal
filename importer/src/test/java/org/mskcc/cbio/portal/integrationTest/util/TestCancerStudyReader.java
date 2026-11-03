/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.util;

import java.io.*;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.util.CancerStudyReader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * JUnit test for CancerStudyReader class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestCancerStudyReader extends IntegrationTestBase {

   @Test
   public void testCancerStudyReaderCancerType() throws Exception {

      File file = new File("src/test/resources/cancer_study.txt");
      CancerStudy cancerStudy = CancerStudyReader.loadCancerStudy( file );
      
      CancerStudy expectedCancerStudy = DaoCancerStudy.getCancerStudyByStableId( "test_brca" );
      assertEquals(expectedCancerStudy, cancerStudy);
      // TBD: change this to use getResourceAsStream()
      file = new File("src/test/resources/cancer_study_bad.txt");
      try {
         cancerStudy = CancerStudyReader.loadCancerStudy( file );
         fail( "Should have thrown DaoException." );
      } catch (Exception e) {
    	 assertEquals("brcaxxx is not a supported cancer type.", e.getMessage());
      }
   }

}
