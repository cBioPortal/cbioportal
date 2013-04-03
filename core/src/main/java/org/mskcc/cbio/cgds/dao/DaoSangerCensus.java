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

package org.mskcc.cbio.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.SangerCancerGene;

/**
 * Data access object for Sanger Cancer Gene Census Table.
 */
public class DaoSangerCensus {
    private static DaoSangerCensus daoSangerCensus;
    private HashMap<String, SangerCancerGene> geneCensus;

    public static DaoSangerCensus getInstance() throws DaoException {
        if (daoSangerCensus == null || daoSangerCensus.getCancerGeneSet().size() == 0) {
            daoSangerCensus = new DaoSangerCensus();
        }
        return daoSangerCensus;
    }

    private DaoSangerCensus() {
    }

    public int addGene(CanonicalGene gene, boolean cancerSomaticMutation, boolean cancerGermlineMutation,
            String tumorTypesSomaticMutation, String tumorTypesGermlineMutation,
            String cancerSyndrome, String tissueType,
            String mutationType, String translocationPartner,
            boolean otherGermlineMut, String otherDisease) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSangerCensus.class);
            pstmt = con.prepareStatement ("INSERT INTO sanger_cancer_census ("
                 + "`ENTREZ_GENE_ID`, `CANCER_SOMATIC_MUT`, `CANCER_GERMLINE_MUT`,"
                 + "`TUMOR_TYPES_SOMATIC_MUT`, `TUMOR_TYPES_GERMLINE_MUT`, `CANCER_SYNDROME`,"
                 + "`TISSUE_TYPE`, `MUTATION_TYPE`, `TRANSLOCATION_PARTNER`, `OTHER_GERMLINE_MUT`,"
                 + "`OTHER_DISEASE`) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
            pstmt.setLong(1, gene.getEntrezGeneId());
            pstmt.setBoolean(2, cancerSomaticMutation);
            pstmt.setBoolean(3, cancerGermlineMutation);
            pstmt.setString(4, tumorTypesSomaticMutation);
            pstmt.setString(5, tumorTypesGermlineMutation);
            pstmt.setString(6, cancerSyndrome);
            pstmt.setString(7, tissueType);
            pstmt.setString(8, mutationType);
            pstmt.setString(9, translocationPartner);
            pstmt.setBoolean(10, otherGermlineMut);
            pstmt.setString(11, otherDisease);
            int rows = pstmt.executeUpdate();
            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSangerCensus.class, con, pstmt, rs);
        }
    }

    public HashMap<String, SangerCancerGene> getCancerGeneSet() throws DaoException {
        if (geneCensus == null || geneCensus.size() == 0) {
            geneCensus = lookUpCancerGeneSet();
        }
        return geneCensus;
    }

    private HashMap<String, SangerCancerGene> lookUpCancerGeneSet() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        HashMap<String, SangerCancerGene> geneCenusus = new HashMap<String, SangerCancerGene>();
        try {
            con = JdbcUtil.getDbConnection(DaoSangerCensus.class);
            pstmt = con.prepareStatement ("SELECT * FROM sanger_cancer_census");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                SangerCancerGene gene = extractCancerGene(rs);
                geneCenusus.put(gene.getGene().getHugoGeneSymbolAllCaps(), gene);
            }
            return geneCenusus;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSangerCensus.class, con, pstmt, rs);
        }
    }

    private SangerCancerGene extractCancerGene(ResultSet rs) throws DaoException, SQLException {
        SangerCancerGene cancerGene = new SangerCancerGene();
        long entrezGene = rs.getLong("ENTREZ_GENE_ID");

        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        cancerGene.setGene(daoGene.getGene(entrezGene));

        cancerGene.setCancerSomaticMutation(rs.getBoolean("CANCER_SOMATIC_MUT"));
        cancerGene.setCancerGermlineMutation(rs.getBoolean("CANCER_GERMLINE_MUT"));
        cancerGene.setTumorTypesSomaticMutation(rs.getString("TUMOR_TYPES_SOMATIC_MUT"));
        cancerGene.setTumorTypesGermlineMutation(rs.getString("TUMOR_TYPES_GERMLINE_MUT"));
        cancerGene.setCancerSyndrome(rs.getString("CANCER_SYNDROME"));
        cancerGene.setTissueType(rs.getString("TISSUE_TYPE"));
        cancerGene.setMutationType(rs.getString("MUTATION_TYPE"));
        cancerGene.setTranslocationPartner(rs.getString("TRANSLOCATION_PARTNER"));
        cancerGene.setOtherGermlineMut(rs.getBoolean("OTHER_GERMLINE_MUT"));
        cancerGene.setOtherDisease(rs.getString("OTHER_DISEASE"));
        return cancerGene;
    }

    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSangerCensus.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE sanger_cancer_census");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSangerCensus.class, con, pstmt, rs);
        }
    }
}