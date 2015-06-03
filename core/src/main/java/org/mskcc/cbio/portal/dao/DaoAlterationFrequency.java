/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author abeshoua
 */
public class DaoAlterationFrequency {
	public static enum AlterationFrequencyType {
		MUT,CNA_AMP,CNA_DEL,MUT_AND_CNA_AMP,MUT_AND_CNA_DEL,WILD_TYPE;
	}
	public static int addAlterationFrequency(int cancerStudyId, long entrezGeneId, int mutCount, int ampCount, int delCount, int mutAmpCount, int mutDelCount, int numPatients)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutationFrequency.class);

            pstmt = con.prepareStatement
                    ("INSERT INTO alteration_frequency (`CANCER_STUDY_ID`,`ENTREZ_GENE_ID`, `MUT`, `AMP`, `DEL`, `MUT_AND_AMP`, `MUT_AND_DEL`, `PATIENTS`) "
                            + "VALUES (?,?,?,?,?,?,?,?)");
            pstmt.setInt(1, cancerStudyId);
	    pstmt.setLong(2, entrezGeneId);
            pstmt.setInt(3, mutCount);
	    pstmt.setInt(4, ampCount);
	    pstmt.setInt(5, delCount);
	    pstmt.setInt(6, mutAmpCount);
	    pstmt.setInt(7, mutDelCount);
	    pstmt.setInt(8, numPatients);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutationFrequency.class, con, pstmt, rs);
        }
    }
}
