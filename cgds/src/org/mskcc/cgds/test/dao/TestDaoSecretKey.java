package org.mskcc.cgds.test.dao;

import junit.framework.TestCase;

import org.mskcc.cgds.dao.DaoSecretKey;
import org.mskcc.cgds.model.SecretKey;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.util.AccessControl;

public class TestDaoSecretKey extends TestCase {

   public void testDaoSecretKey() throws Exception {
      DaoSecretKey.deleteAllRecords();

      assertEquals( 0, DaoSecretKey.getAllSecretKeys().size() );

      AccessControl.createSecretKey("abcd");
      assertEquals( 1, DaoSecretKey.getAllSecretKeys().size() );

      DaoSecretKey.deleteAllRecords();
      assertEquals( 0, DaoSecretKey.getAllSecretKeys().size() );

      SecretKey secretKey = AccessControl.createSecretKey("abcd");
      assertEquals( 1, DaoSecretKey.getAllSecretKeys().size() );

      AccessControl.createSecretKey("abcd");
      assertEquals( 2, DaoSecretKey.getAllSecretKeys().size() );

      DaoSecretKey.deleteSecretKey(secretKey);
      assertEquals( 1, DaoSecretKey.getAllSecretKeys().size() );

   }
}
