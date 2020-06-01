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

package org.mskcc.cbio.portal.dao;

import java.util.Calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoTextCache;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestDaoTextCache
{

	@Test
	public void testDaoCaseList() throws DaoException
	{
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
