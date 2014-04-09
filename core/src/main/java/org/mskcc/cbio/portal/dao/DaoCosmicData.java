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

package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.portal.model.CosmicMutationFrequency;
import org.mskcc.cbio.portal.model.ExtendedMutation;

/**
 *
 * @author jgao
 */
public class DaoCosmicData {
    public static int addCosmic(CosmicMutationFrequency cosmic) throws DaoException {
            if (!MySQLbulkLoader.isBulkLoad()) {
                throw new DaoException("You have to turn on MySQLbulkLoader in order to insert mutations");
            } else {

                    // use this code if bulk loading
                    // write to the temp file maintained by the MySQLbulkLoader
                    MySQLbulkLoader.getMySQLbulkLoader("cosmic_mutation").insertRecord(
                            cosmic.getId(),
                            cosmic.getChr(),
                            Long.toString(cosmic.getStartPosition()),
                            cosmic.getReferenceAllele(),
                            cosmic.getTumorSeqAllele(),
                            cosmic.getStrand(),
                            cosmic.getCds(),
                            Long.toString(cosmic.getEntrezGeneId()),
                            cosmic.getAminoAcidChange(),
                            Integer.toString(cosmic.getFrequency()),
                            cosmic.getKeyword());

                    return 1;
            }
    }
    
    /**
     * 
     * @param mutations
     * @return Map of event id to map of aa change to count
     * @throws DaoException 
     */
    public static Map<Long, Set<CosmicMutationFrequency>> getCosmicForMutationEvents(
            List<ExtendedMutation> mutations) throws DaoException {
        Set<String> mutKeywords = new HashSet<String>();
        for (ExtendedMutation mut : mutations) {
            mutKeywords.add(mut.getKeyword());
        }
        
        Map<String, Set<CosmicMutationFrequency>> map = 
                DaoCosmicData.getCosmicDataByKeyword(mutKeywords);
        Map<Long, Set<CosmicMutationFrequency>> ret
                = new HashMap<Long, Set<CosmicMutationFrequency>>(map.size());
        for (ExtendedMutation mut : mutations) {
            String keyword = mut.getKeyword();
            Set<CosmicMutationFrequency> cmfs = filterTruncatingCosmicByPosition(mut, map.get(keyword));
            
            if (cmfs==null || cmfs.isEmpty()) {
                continue;
            }
            
            ret.put(mut.getMutationEventId(), cmfs);
        }
        return ret;
    }
    
    private static Set<CosmicMutationFrequency> filterTruncatingCosmicByPosition(
            ExtendedMutation mut, Set<CosmicMutationFrequency> cmfs) {
        if (!mut.getKeyword().endsWith("truncating") || cmfs==null) {
            return cmfs;
        }
        
        Set<CosmicMutationFrequency> ret = new HashSet<CosmicMutationFrequency>();
        Pattern p = Pattern.compile("[0-9]+");
        int mutPos = mut.getOncotatorProteinPosStart();
        for (CosmicMutationFrequency cmf : cmfs) {
            String aa = cmf.getAminoAcidChange();
            Matcher m = p.matcher(aa);
            if (m.find()) {
                int cmfPos = Integer.parseInt(m.group());
                if (mutPos==cmfPos) {
                    ret.add(cmf);
                }
            }
        }
        return ret;
    }
    
    /**
     * 
     * @param keywordS
     * @return Map<keyword, List<cosmic>>
     * @throws DaoException 
     */
    public static Map<String,Set<CosmicMutationFrequency>> getCosmicDataByKeyword(Collection<String> keywordS) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCosmicData.class);
            pstmt = con.prepareStatement("SELECT * FROM cosmic_mutation "
                    + " WHERE KEYWORD in ('" + StringUtils.join(keywordS, "','") + "')");
            rs = pstmt.executeQuery();
            Map<String,Set<CosmicMutationFrequency>> ret = new HashMap<String,Set<CosmicMutationFrequency>>();
            while (rs.next()) {
                CosmicMutationFrequency cmf = extractCosmic(rs);
                Set<CosmicMutationFrequency> cmfs = ret.get(cmf.getKeyword());
                if (cmfs==null) {
                    cmfs = new HashSet<CosmicMutationFrequency>();
                    ret.put(cmf.getKeyword(), cmfs);
                }
                cmfs.add(cmf);
            }
            return ret;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCosmicData.class, con, pstmt, rs);
        }
    }
    
    private static CosmicMutationFrequency extractCosmic(ResultSet rs) throws SQLException {
        String id = rs.getString("COSMIC_MUTATION_ID");
        long entrez = rs.getLong("ENTREZ_GENE_ID");
        String aa = rs.getString("PROTEIN_CHANGE");
        String keyword = rs.getString("KEYWORD");
        int count = rs.getInt("COUNT");
        return new CosmicMutationFrequency(id, entrez, aa, keyword, count);
    }
    
    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCosmicData.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE cosmic_mutation");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCosmicData.class, con, pstmt, rs);
        }
    }
}
