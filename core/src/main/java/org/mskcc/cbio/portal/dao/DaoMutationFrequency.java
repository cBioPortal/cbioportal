/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.CanonicalGene;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Data access object for Mutation Frequency table
 */
public class DaoMutationFrequency {

    public int addGene(long entrezGeneId, double mutationFrequency, int cancerStudyId)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutationFrequency.class);

            pstmt = con.prepareStatement
                    ("INSERT INTO mutation_frequency (`ENTREZ_GENE_ID`,`SOMATIC_MUTATION_RATE`, `CANCER_STUDY_ID`) "
                            + "VALUES (?,?,?)");
            pstmt.setLong(1, entrezGeneId);
            pstmt.setDouble(2, mutationFrequency);
            pstmt.setInt(3, cancerStudyId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutationFrequency.class, con, pstmt, rs);
        }
    }

    public ArrayList<CanonicalGene> getTop100SomaticMutatedGenes( int cancerStudyId) throws DaoException {
        ArrayList <CanonicalGene> geneList = new ArrayList<CanonicalGene>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        try {
            con = JdbcUtil.getDbConnection(DaoMutationFrequency.class);

            pstmt = con.prepareStatement
                    ("SELECT * FROM mutation_frequency WHERE CANCER_STUDY_ID = ? " +
                            "ORDER BY SOMATIC_MUTATION_RATE DESC LIMIT 0, 100");
            pstmt.setInt(1, cancerStudyId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                long entrezGeneId = rs.getLong("ENTREZ_GENE_ID");
                CanonicalGene gene = daoGene.getGene(entrezGeneId);
                gene.setSomaticMutationFrequency(rs.getDouble("SOMATIC_MUTATION_RATE"));
                geneList.add(gene);
            }
            return geneList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutationFrequency.class, con, pstmt, rs);
        }
    }

    public CanonicalGene getSomaticMutationFrequency(long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        try {
            con = JdbcUtil.getDbConnection(DaoMutationFrequency.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM mutation_frequency WHERE ENTREZ_GENE_ID=?");
            pstmt.setLong(1, entrezGeneId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                CanonicalGene gene = daoGene.getGene(entrezGeneId);
                gene.setSomaticMutationFrequency(rs.getDouble("SOMATIC_MUTATION_RATE"));
                return gene;
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutationFrequency.class, con, pstmt, rs);
        }
    }

    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutationFrequency.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE mutation_frequency");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutationFrequency.class, con, pstmt, rs);
        }
    }
}