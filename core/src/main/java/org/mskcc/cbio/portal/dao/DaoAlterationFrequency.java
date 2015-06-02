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
	public static int addAlterationFrequency(int cancerStudyId, long entrezGeneId, AlterationFrequencyType freqType, int frequency, int numPatients)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutationFrequency.class);

            pstmt = con.prepareStatement
                    ("INSERT INTO alteration_frequency (`CANCER_STUDY_ID`,`ENTREZ_GENE_ID`, `ALTERATION_TYPE`, `FREQUENCY`, `PATIENTS`) "
                            + "VALUES (?,?,?,?,?)");
            pstmt.setInt(1, cancerStudyId);
	    pstmt.setLong(2, entrezGeneId);
            pstmt.setString(3, freqType.toString());
	    pstmt.setInt(4, frequency);
	    pstmt.setInt(5, numPatients);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutationFrequency.class, con, pstmt, rs);
        }
    }
}
