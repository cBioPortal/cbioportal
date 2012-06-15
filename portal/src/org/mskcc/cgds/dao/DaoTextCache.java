package org.mskcc.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.mskcc.portal.util.CacheUtil;

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
			con = JdbcUtil.getDbConnection();
			pstmt = con.prepareStatement(
					"INSERT INTO text_cache (`HASH_KEY`, `TEXT`, `DATE_TIME_STAMP`) "
			        		+ "VALUES (?,?,NOW())");
			pstmt.setString(1, key);
			pstmt.setString(2, text);

			// TODO use java date instenad of db's native NOW() function?
//			Date date = new Date();
//			Object dateTime = new Timestamp(date.getTime());
//			pstmt.setObject(3, dateTime); 
			
			int rows = pstmt.executeUpdate();
			
			return rows;
        }
        catch (SQLException e)
        {
        	throw new DaoException(e);
        }
        finally
        {
            JdbcUtil.closeAll(con, pstmt, rs);
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
            con = JdbcUtil.getDbConnection();
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
            JdbcUtil.closeAll(con, pstmt, rs);
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
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE text_cache");
            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new DaoException(e);
        } 
        finally
        {
            JdbcUtil.closeAll(con, pstmt, rs);
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
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("DELETE FROM text_cache " +
            		"WHERE `DATE_TIME_STAMP` <= ?");
            
            // create date_time_stamp string using the given date
            // (java.sql package does not have a proper "datetime" type support)
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            
            pstmt.setString(1, formatter.format(date));
            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new DaoException(e);
        } 
        finally
        {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}
