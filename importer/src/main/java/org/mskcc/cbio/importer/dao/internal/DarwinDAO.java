/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/

package org.mskcc.cbio.importer.dao.internal;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO for darwin data.
 */
public class DarwinDAO
{
	private BasicDataSource dataSource;

	// TODO implement methods to get required data

	public BasicDataSource getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(BasicDataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	// TODO return a list of results
	public void getAllClinicalData()
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			con = this.dataSource.getConnection();

			pstmt = con.prepareStatement("select *\n" +
				"from dbo.[CDB Tumor P1] p1\n" +
				"join dbo.[CDB Tumor P2] p2 on " +
				"p1.[Deident PT #] = p2.[Deident PT #] and p1.TM_TUMOR_SEQ = p2.TM_TUMOR_SEQ\n" +
				"join dbo.[CDB Tumor P3] p3 on " +
				"p1.[Deident PT #] = p3.[Deident PT #] and p1.TM_TUMOR_SEQ = p3.TM_TUMOR_SEQ\n");

			rs = pstmt.executeQuery();

			while (rs.next())
			{
				//TODO generate the list
			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
