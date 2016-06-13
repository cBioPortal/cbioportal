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

import org.mskcc.cbio.portal.model.ProteinArrayInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jj
 */
public class DaoProteinArrayInfo {
    private static DaoProteinArrayInfo daoProteinArrayInfo;

    /**
     * Private Constructor to enforce Singleton Pattern.
     */
    private DaoProteinArrayInfo() {
    }

    /**
     * Gets Global Singleton Instance.
     *
     * @return DaoProteinArrayInfo Singleton.
     * @throws DaoException Database Error.
     */
    public static DaoProteinArrayInfo getInstance() throws DaoException {
        if (daoProteinArrayInfo == null) {
            daoProteinArrayInfo = new DaoProteinArrayInfo();
        }
        
        return daoProteinArrayInfo;
    }

    /**
     * Adds a new ProteinArrayInfo Record to the Database.
     *
     * @param pai ProteinArrayInfo Object.
     * @return number of records successfully added.
     * @throws DaoException Database Error.
     */
    public int addProteinArrayInfo(ProteinArrayInfo pai) throws DaoException {
        if (this.getProteinArrayInfo(pai.getId())!=null) {
            System.err.println("Protein array "+pai.getId()+" has alread been added. Ignore!");
            return 0;
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayInfo.class);
            pstmt = con.prepareStatement
                    ("INSERT INTO protein_array_info (`PROTEIN_ARRAY_ID`,`TYPE`,`GENE_SYMBOL`,`TARGET_RESIDUE`) "
                            + "VALUES (?,?,?,?)");
            pstmt.setString(1, pai.getId());
            pstmt.setString(2, pai.getType());
            pstmt.setString(3, pai.getGene());
            pstmt.setString(4, pai.getResidue());
            return pstmt.executeUpdate() + addProteinArrayCancerStudy(pai.getId(), pai.getCancerStudies());
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayInfo.class, con, pstmt, rs);
        }
    }
    
    public int deleteProteinArrayInfo(String arrayId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayInfo.class);
            pstmt = con.prepareStatement
                    ("DELETE FROM protein_array_info WHERE `PROTEIN_ARRAY_ID`=? ");
            pstmt.setString(1, arrayId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayInfo.class, con, pstmt, rs);
        }
    }
    
    public int addProteinArrayCancerStudy(String arrayId, Set<Integer> cancerStudyIds) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayInfo.class);
            int rows = 0;
            for (int cancerStudyId : cancerStudyIds) {
                if (proteinArrayCancerStudyAdded(arrayId, cancerStudyId)) {
                    System.err.println("RPPA array "+arrayId+" has already been added for cancer study of "
                            + cancerStudyId + ".");
                    continue;
                }
                
                pstmt = con.prepareStatement
                        ("INSERT INTO protein_array_cancer_study (`PROTEIN_ARRAY_ID`,`CANCER_STUDY_ID`) "
                                + "VALUES (?,?)");
                pstmt.setString(1, arrayId);
                pstmt.setInt(2, cancerStudyId);
                rows += pstmt.executeUpdate();
            }
            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayInfo.class, con, pstmt, rs);
        }
    }
    
    public boolean proteinArrayCancerStudyAdded(String arrayId, int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayInfo.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM protein_array_cancer_study WHERE PROTEIN_ARRAY_ID=? AND CANCER_STUDY_ID=?");
            pstmt.setString(1, arrayId);
            pstmt.setInt(2, cancerStudyId);
            
            rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayInfo.class, con, pstmt, rs);
        }
    }

    /**
     * Gets the ProteinArrayInfo with the Specified array ID.
     *
     * @param arrayId protein array ID.
     * @return ProteinArrayInfo Object.
     * @throws DaoException Database Error.
     */
    public ProteinArrayInfo getProteinArrayInfo(String arrayId) throws DaoException {
        ArrayList<ProteinArrayInfo> pais = getProteinArrayInfo(Collections.singleton(arrayId), null);
        if (pais.isEmpty()) {
            return null;
        }
        
        return pais.get(0);
    }
    
    public ArrayList<ProteinArrayInfo> getProteinArrayInfo(Collection<String> arrayIds, Collection<String> types)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList<ProteinArrayInfo> pais = new ArrayList<ProteinArrayInfo>();
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayInfo.class);
            if (types==null) {
                pstmt = con.prepareStatement
                        ("SELECT * FROM protein_array_info WHERE PROTEIN_ARRAY_ID in ('"
                            +StringUtils.join(arrayIds, "','")+"')");
            } else {
                
                pstmt = con.prepareStatement
                        ("SELECT * FROM protein_array_info WHERE TYPE in ('"
                        + StringUtils.join(types, "','")+"')"
                        + " AND PROTEIN_ARRAY_ID in ('"+StringUtils.join(arrayIds, "','")+"')");
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String arrayId = rs.getString("PROTEIN_ARRAY_ID");
                ProteinArrayInfo pai = new ProteinArrayInfo(arrayId,
                        rs.getString("TYPE"),
                        rs.getString("GENE_SYMBOL"),
                        rs.getString("TARGET_RESIDUE"),
                        getCancerTypesOfArray(arrayId, con));
                pais.add(pai);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayInfo.class, con, pstmt, rs);
        }
        
        return pais;
    }
    
    public ArrayList<ProteinArrayInfo> getProteinArrayInfo(int cancerStudyId) throws DaoException {
        return getProteinArrayInfoForType(cancerStudyId, null);
    }

    /**
     * Gets all Protein array information in the Database.
     *
     * @return ArrayList of ProteinArrayInfoes.
     * @throws DaoException Database Error.
     */
    public ArrayList<ProteinArrayInfo> getProteinArrayInfoForType(int cancerStudyId, 
            Collection<String> types) throws DaoException {
        ArrayList<ProteinArrayInfo> list = new ArrayList<ProteinArrayInfo>();
        
        Set<String> arrayIds = getArrayIdsOfCancerType(cancerStudyId);
        
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayInfo.class);
            if (types==null) {
                pstmt = con.prepareStatement
                        ("SELECT * FROM protein_array_info WHERE PROTEIN_ARRAY_ID in ('"
                            +StringUtils.join(arrayIds, "','")+"')");
            } else {
                pstmt = con.prepareStatement
                        ("SELECT * FROM protein_array_info WHERE TYPE in ('"
                        +StringUtils.join(types, "','")+"') AND PROTEIN_ARRAY_ID in ('"
                            +StringUtils.join(arrayIds, "','")+"')");
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String arrayId = rs.getString("PROTEIN_ARRAY_ID");
                ProteinArrayInfo pai = new ProteinArrayInfo(arrayId,
                        rs.getString("TYPE"),
                        rs.getString("GENE_SYMBOL"),
                        rs.getString("TARGET_RESIDUE"),
                        getCancerTypesOfArray(arrayId, con));
                list.add(pai);
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayInfo.class, con, pstmt, rs);
        }
    }

    /**
     * Gets all Protein array information in the Database.
     *
     * @return ArrayList of ProteinArrayInfoes.
     * @throws DaoException Database Error.
     */
    public ArrayList<ProteinArrayInfo> getAllProteinArrayInfo() throws DaoException {
        ArrayList<ProteinArrayInfo> list = new ArrayList<ProteinArrayInfo>();
        
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayInfo.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM protein_array_info");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String arrayId = rs.getString("PROTEIN_ARRAY_ID");
                ProteinArrayInfo pai = new ProteinArrayInfo(arrayId,
                        rs.getString("TYPE"),
                        rs.getString("GENE_SYMBOL"),
                        rs.getString("TARGET_RESIDUE"),
                        getCancerTypesOfArray(arrayId, con));
                list.add(pai);
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayInfo.class, con, pstmt, rs);
        }
    }
    
    public ArrayList<ProteinArrayInfo> getProteinArrayInfoForEntrezId(int cancerStudyId, long entrezId, Collection<String> types) throws DaoException {
        return getProteinArrayInfoForEntrezIds(cancerStudyId, Collections.singleton(entrezId), types);
    }
    
    public ArrayList<ProteinArrayInfo> getProteinArrayInfoForEntrezIds(int cancerStudyId, Collection<Long> entrezIds, Collection<String> types) throws DaoException {
        Set<String> arrayIds = getArrayIdsOfCancerType(cancerStudyId);
        arrayIds.retainAll(DaoProteinArrayTarget.getInstance().getProteinArrayIds(entrezIds));
        return getProteinArrayInfo(arrayIds, types);
    }
    
    public Set<String> getAllAntibodyTypes() throws DaoException {
        Set<String> ret = new HashSet<String>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayInfo.class);
            pstmt = con.prepareStatement
                        ("SELECT DISTINCT TYPE FROM protein_array_info");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ret.add(rs.getString(1));
            }
            return ret;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayInfo.class, con, pstmt, rs);
        }
        
    }
    
    private Set<Integer> getCancerTypesOfArray(String arrayId, Connection con) throws DaoException {
        PreparedStatement pstmt;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement
                    ("SELECT CANCER_STUDY_ID FROM protein_array_cancer_study WHERE PROTEIN_ARRAY_ID=?");
            pstmt.setString(1, arrayId);
            
            Set<Integer> set = new HashSet<Integer>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                set.add(rs.getInt(1));
            }
            
            return set;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(rs);
        }
    }
    
    private Set<String> getArrayIdsOfCancerType(int cancerTypeId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayInfo.class);
            pstmt = con.prepareStatement
                    ("SELECT PROTEIN_ARRAY_ID FROM protein_array_cancer_study WHERE CANCER_STUDY_ID=?");
            pstmt.setInt(1, cancerTypeId);
            
            Set<String> set = new HashSet<String>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                set.add(rs.getString(1));
            }
            
            return set;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayInfo.class, con, pstmt, rs);
        }
    }    

    /**
     * Deletes all protein array info Records in the Database.
     *
     * @throws DaoException Database Error.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayInfo.class);
            JdbcUtil.disableForeignKeyCheck(con);
            pstmt = con.prepareStatement("TRUNCATE TABLE protein_array_info");
            pstmt.executeUpdate();
            JdbcUtil.enableForeignKeyCheck(con);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayInfo.class, con, pstmt, rs);
        }
    }
}
