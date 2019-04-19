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

import org.mskcc.cbio.portal.model.ReferenceGenomeGene;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object to Reference Genome Gene Table
 * Make a Singleton Class accessed by all objects throughout the system
 * @author Kelsey Zhu
 */
public class DaoReferenceGenomeGene {
    private static DaoReferenceGenomeGene instance = null;
    
    public static DaoReferenceGenomeGene getInstance() {
        if (instance == null) {
            instance = new DaoReferenceGenomeGene();
        }
        return instance;
    }
    
    /**
     * Update Reference Genome Gene in the Database.
     * @param gene Reference Genome Gene 
     */
    public void updateGene(ReferenceGenomeGene gene) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            MySQLbulkLoader.bulkLoadOff();
            con = JdbcUtil.getDbConnection(DaoReferenceGenome.class);
            pstmt = con.prepareStatement
                ("UPDATE reference_genome_gene SET `EXONIC_LENGTH`=?,`START`=?, `END`=? WHERE `ENTREZ_GENE_ID`=? AND `REFERENCE_GENOME_ID`=?");
            pstmt.setInt(1, gene.getExonicLength());
            pstmt.setLong(2, gene.getStart());
            pstmt.setLong(3, gene.getEnd());
            pstmt.setLong(4, gene.getEntrezGeneId());
            pstmt.setInt(5, gene.getReferenceGenomeId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            MySQLbulkLoader.bulkLoadOn();
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }

    /**
     * Adds a new reference genome gene to the Database or update the existing one.
     * @param gene Reference Genome Gene Object.
     * @throws DaoException Database Error.
     */
    public void addOrUpdateGene(ReferenceGenomeGene gene) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            ReferenceGenomeGene existingGene = getGene(gene.getEntrezGeneId(), gene.getReferenceGenomeId());

            if (existingGene == null) {
                //add gene, referring to this genetic entity
                con = JdbcUtil.getDbConnection(DaoGene.class);
                pstmt = con.prepareStatement
                    ("INSERT INTO `reference_genome_gene` (`ENTREZ_GENE_ID`, `REFERENCE_GENOME_ID`,`CHR`,`CYTOBAND`,`EXONIC_LENGTH`,`START`,`END`) "
                        + "VALUES (?,?,?,?,?,?,?)");
                pstmt.setLong(1, gene.getEntrezGeneId());
                pstmt.setInt(2, gene.getReferenceGenomeId());
                pstmt.setString(3, gene.getChr());
                pstmt.setString(4, gene.getCytoband());
                pstmt.setInt(5, gene.getExonicLength());
                pstmt.setLong(6, gene.getStart());
                pstmt.setLong(7, gene.getEnd());
                pstmt.executeUpdate();
            }
            else {
                updateGene(existingGene);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }


    /**
     * Gets the Reference Genome Gene with the Specified Entrez Gene ID.
     * @param entrezGeneId ENTRZ GENE ID.
     * @return Reference Genome Gene Object.
     * @throws DaoException Database Error.
     */
    public ReferenceGenomeGene getGene(long entrezGeneId, int referenceGenomeId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoReferenceGenomeGene.class);
            pstmt = con.prepareStatement
                ("SELECT * FROM `reference_genome_gene` WHERE `ENTREZ_GENE_ID` = ? AND `REFERENCE_GENOME_ID` = ?");
            pstmt.setLong(1, entrezGeneId);
            pstmt.setInt(2, referenceGenomeId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractGene(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }

    private ReferenceGenomeGene extractGene(ResultSet rs) throws SQLException, DaoException {
        int entrezGeneId = rs.getInt("ENTREZ_GENE_ID");
        int reference_genome_id = rs.getInt("REFERENCE_GENOME_ID");
        String cytoband = rs.getString("CYTOBAND");
        ReferenceGenomeGene gene = new ReferenceGenomeGene(entrezGeneId, reference_genome_id);
        gene.setChr(rs.getString("CHR"));
        gene.setCytoband(rs.getString("CYTOBAND"));
        gene.setExonicLength(rs.getInt("EXONIC_LENGTH"));
        gene.setStart(rs.getLong("START"));
        gene.setEnd(rs.getLong("END"));
        return gene;
    }

    /**
     * Deletes the Reference Genome Gene for a given Entrez Gene ID and Referece Genome ID.
     * @param entrezGeneId ENTREZ GENE ID 
     * @param referenceGenomeId REFERENCE GENOME ID
     */
    public void deleteGene(int entrezGeneId, int referenceGenomeId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoReferenceGenomeGene.class);
            pstmt = con.prepareStatement("DELETE FROM `reference_genome_gene` WHERE ENTREZ_GENE_ID=? AND REFERENCE_GENOME_ID=?");
            pstmt.setLong(1, entrezGeneId);
            pstmt.setInt(2, referenceGenomeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
}
