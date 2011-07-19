package org.mskcc.cgds.test.web_api;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.web_api.GetTypesOfCancer;
import org.mskcc.cgds.web_api.ProtocolException;

import junit.framework.TestCase;

public class TestGetTypesOfCancer extends TestCase {

   public void testGetTypesOfCancer() throws DaoException, Exception, ProtocolException {
      ResetDatabase.resetDatabase();
      // load cancers
      String[] args = { "data/cancers.txt" };
      ImportTypesOfCancers.main( args );
      
      String output = GetTypesOfCancer.getTypesOfCancer();
      assertTrue(output.contains("GBM\tGlioblastoma multiforme"));
      assertTrue(output.contains("PRAD\tProstate adenocarcinoma"));
   }

}
