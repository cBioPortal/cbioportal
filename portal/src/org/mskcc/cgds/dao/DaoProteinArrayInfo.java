
package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.ProteinArrayInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.io.IOException;
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
    // use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
    private static MySQLbulkLoader myMySQLbulkLoader = null;
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

        if (myMySQLbulkLoader == null) {
            myMySQLbulkLoader = new MySQLbulkLoader("protein_array_info");
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
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (MySQLbulkLoader.isBulkLoad()) {
                //  write to the temp file maintained by the MySQLbulkLoader
                myMySQLbulkLoader.insertRecord(pai.getId(),
                        pai.getType(), pai.getSource(), Boolean.toString(pai.isValidated()));
                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            } else {
                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement("INSERT INTO protein_array_info "
                           + "(`PROTEIN_ARRAY_ID`,`TYPE`,`SOURCE_ORGANISM`,`GENE_SYMBOL`,`TARGET_RESIDUE`,`VALIDATED`) "
                           + "VALUES (?,?,?,?,?,?)");
                pstmt.setString(1, pai.getId());
                pstmt.setString(2, pai.getType());
                pstmt.setString(3, pai.getSource());
                pstmt.setString(4, pai.getGene());
                pstmt.setString(5, pai.getResidue());
                pstmt.setBoolean(6, pai.isValidated());
                int rows = pstmt.executeUpdate() + addProteinArrayCancerStudy(pai);
                return rows;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    public int addProteinArrayCancerStudy(ProteinArrayInfo pai) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (MySQLbulkLoader.isBulkLoad()) {
                //  write to the temp file maintained by the MySQLbulkLoader
                myMySQLbulkLoader.insertRecord(pai.getId(),
                        pai.getType(), pai.getSource(), Boolean.toString(pai.isValidated()));
                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            } else {
                con = JdbcUtil.getDbConnection();
                int rows = 0;
                for (int cancerStudyId : pai.getCancerStudies()) {
                    pstmt = con.prepareStatement
                            ("INSERT INTO protein_array_cancer_study (`PROTEIN_ARRAY_ID`,`CANCER_STUDY_ID`) "
                                    + "VALUES (?,?)");
                    pstmt.setString(1, pai.getId());
                    pstmt.setInt(2, cancerStudyId);
                    rows += pstmt.executeUpdate();
                }
                return rows;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Loads the temp file maintained by the MySQLbulkLoader into the DMBS.
     *
     * @return number of records inserted
     * @throws DaoException Database Error.
     */
    public int flushProteinArrayInfoesToDatabase() throws DaoException {
        try {
            return myMySQLbulkLoader.loadDataFromTempFileIntoDBMS();
        } catch (IOException e) {
            System.err.println("Could not open temp file");
            e.printStackTrace();
            return -1;
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
            con = JdbcUtil.getDbConnection();
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
                        rs.getString("SOURCE_ORGANISM"),
                        rs.getString("GENE_SYMBOL"),
                        rs.getString("TARGET_RESIDUE"),
                        rs.getBoolean("VALIDATED"),
                        getCancerTypesOfArray(arrayId));
                pais.add(pai);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
        
        return pais;
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
            con = JdbcUtil.getDbConnection();
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
                        rs.getString("SOURCE_ORGANISM"),
                        rs.getString("GENE_SYMBOL"),
                        rs.getString("TARGET_RESIDUE"),
                        rs.getBoolean("VALIDATED"),
                        getCancerTypesOfArray(arrayId));
                list.add(pai);
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
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
            con = JdbcUtil.getDbConnection();
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
            JdbcUtil.closeAll(con, pstmt, rs);
        }
        
    }
    
    private Set<Integer> getCancerTypesOfArray(String arrayId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
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
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    private Set<String> getArrayIdsOfCancerType(int cancerTypeId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
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
            JdbcUtil.closeAll(con, pstmt, rs);
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
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE protein_array_info");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}
