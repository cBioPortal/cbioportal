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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.portal.model.Coexpression;

/**
 *
 * @author jgao
 */
public class DaoCoexpression {
    public static int addCoexpression(Coexpression coexpression) throws DaoException {
            if (!MySQLbulkLoader.isBulkLoad()) {
                throw new DaoException("You have to turn on MySQLbulkLoader in order to insert Coexpression data");
            } else {
                    // use this code if bulk loading
                    // write to the temp file maintained by the MySQLbulkLoader
                    MySQLbulkLoader.getMySQLbulkLoader("coexpression").insertRecord(
                            Long.toString(coexpression.getGene1()),
                            Long.toString(coexpression.getGene2()), 
                            Integer.toString(coexpression.getProfileId()),
                            Double.toString(coexpression.getPearson()),
                            Double.toString(coexpression.getSpearman()));
                    return 1;
            }
    }
    
    public static ArrayList<Coexpression> getCoexpression(Collection<Long> queryGenes, int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCoexpression.class);
            ArrayList<Coexpression> result = new ArrayList<Coexpression>();
            pstmt = con.prepareStatement("SELECT * FROM coexpression WHERE " +
                    "GENETIC_PROFILE_ID='" + geneticProfileId + "' AND " +
                    "(GENE_1 in ('" + StringUtils.join(queryGenes, "','") + "')" + " OR " +
                    " GENE_2 in ('" + StringUtils.join(queryGenes, "','") + "'));");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int gene1Id = Integer.parseInt(rs.getString("GENE_1"));
                int gene2Id = Integer.parseInt(rs.getString("GENE_2"));
                //int geneticProfileId = Integer.parseInt(rs.getString("GENETIC_PROFILE_ID"));
                float pearson = Float.parseFloat(rs.getString("PEARSON"));
                float spearman = Float.parseFloat(rs.getString("SPEARMAN"));
                result.add(new Coexpression(gene1Id, gene2Id, geneticProfileId, pearson, spearman));
            }
            return result;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCoexpression.class, con, pstmt, rs);
        }
    }

}
