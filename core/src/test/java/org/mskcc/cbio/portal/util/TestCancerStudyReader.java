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

package org.mskcc.cbio.portal.util;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

/**
 * JUnit test for CancerStudyReader class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestCancerStudyReader {

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

	@Test
	   public void testCancerStudyReaderShortName() throws Exception {

	      File file = new File("src/test/resources/cancer_study.txt");
	      CancerStudy cancerStudy = CancerStudyReader.loadCancerStudy( file );
	      
	      CancerStudy expectedCancerStudy = DaoCancerStudy.getCancerStudyByStableId( "test_brca" );
	      assertEquals(expectedCancerStudy, cancerStudy);
	      // TBD: change this to use getResourceAsStream()
	      file = new File("src/test/resources/cancer_study_bad_short_name.txt");
	      try {
	         cancerStudy = CancerStudyReader.loadCancerStudy( file );
	         fail( "Should have thrown DaoException." );
	      } catch (Exception e) {
	    	 assertEquals("short_name is not specified.", e.getMessage());
	      }
	   }
}
