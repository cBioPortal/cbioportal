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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.mskcc.cbio.portal.util.CacheUtil;

public class DaoTextCache
{
	/**
	 * Generates an MD5 key for the given text.
	 *
	 * @param text	text to hashed
	 * @return		an MD5 key corresponding to the given text
	 */
	public String generateKey(String text)
	{
		return CacheUtil.md5sum(text);
	}

	/**
	 * Inserts the given key and text pair to the database.
	 *
	 * @param key			key value
	 * @param text			text value
	 * @return
	 * @throws DaoException	if an entity already exists with the same key
	 */
	public int cacheText(String key, String text) throws DaoException
	{
		Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try
        {
			con = JdbcUtil.getDbConnection(DaoTextCache.class);
			pstmt = con.prepareStatement(
					"INSERT INTO text_cache (`HASH_KEY`, `TEXT`, `DATE_TIME_STAMP`) "
			        		+ "VALUES (?,?,NOW())");
			pstmt.setString(1, key);
			pstmt.setString(2, text);
			return pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
        	throw new DaoException(e);
        }
        finally
        {
            JdbcUtil.closeAll(DaoTextCache.class, con, pstmt, rs);
        }
	}

	/**
	 * Retrieves the text corresponding to the given key form the DB.
	 *
	 * @param key	cache key
	 * @return
	 * @throws DaoException
	 */
    public String getText(String key) throws DaoException
    {
    	Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try
        {
            con = JdbcUtil.getDbConnection(DaoTextCache.class);
            pstmt = con.prepareStatement("SELECT * FROM text_cache " +
                    "WHERE HASH_KEY=?");
            pstmt.setString(1, key);
            rs = pstmt.executeQuery();
            if (rs.next())
            {
                return rs.getString("TEXT");
            }
            return null;
        }
        catch (SQLException e)
        {
            throw new DaoException(e);
        }
        finally
        {
            JdbcUtil.closeAll(DaoTextCache.class, con, pstmt, rs);
        }
    }

    /**
     * Deletes all records in the table.
     *
     * @throws DaoException
     */
    public void deleteAllKeys() throws DaoException
    {
    	Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try
        {
            con = JdbcUtil.getDbConnection(DaoTextCache.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE text_cache");
            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new DaoException(e);
        }
        finally
        {
            JdbcUtil.closeAll(DaoTextCache.class, con, pstmt, rs);
        }
    }

    /**
     * Remove records older than the specified date.
     *
     * @param date	threshold date
     * @throws DaoException
     */
    public void purgeOldKeys(Date date) throws DaoException
    {
    	Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try
        {
            con = JdbcUtil.getDbConnection(DaoTextCache.class);
            pstmt = con.prepareStatement("DELETE FROM text_cache " +
            		"WHERE `DATE_TIME_STAMP` <= ?");
            // create date_time_stamp string using the given date
            // (java.sql package does not have a proper "datetime" type support)
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            formatter.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            pstmt.setString(1, formatter.format(date));
            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new DaoException(e);
        }
        finally
        {
            JdbcUtil.closeAll(DaoTextCache.class, con, pstmt, rs);
        }
    }
}
