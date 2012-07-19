package org.mskcc.cbio.cgds.test.dao;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import junit.framework.TestCase;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoTextCache;
import org.mskcc.cgds.scripts.ResetDatabase;

public class TestDaoTextCache extends TestCase
{
	public void testDaoCaseList() throws DaoException
	{
        ResetDatabase.resetDatabase();
        DaoTextCache daoTextCache = new DaoTextCache();

        String text1 = "even a single character change should make a big difference";
        String text2 = "even a single character chance should make a big difference";
        
        // generate keys for the texts
        String key1 = daoTextCache.generateKey(text1);
        String key2 = daoTextCache.generateKey(text2);
        
        // generated keys should be different
        assertFalse(key1.equals(key2));
        
        // cache texts
        daoTextCache.cacheText(key1, text1);
        daoTextCache.cacheText(key2, text2);
        
        // get texts from database and ensure returned values are correct
        assertEquals(text1, daoTextCache.getText(key1));
        assertEquals(text2, daoTextCache.getText(key2));

        // delete anything older than current time
        daoTextCache.purgeOldKeys(Calendar.getInstance().getTime());
        
        // assert that both keys are deleted
        assertEquals(null, daoTextCache.getText(key1));
        assertEquals(null, daoTextCache.getText(key2));
    }
}
