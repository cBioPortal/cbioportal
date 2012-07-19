package org.mskcc.cbio.cgds.dao;

import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.Gistic;
import org.mskcc.cgds.validate.ValidateGistic;
import org.mskcc.cgds.validate.validationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * A gistic includes a ROI and GISTIC information concerning that region.
 *
 * @author Gideon Dresdner
 */

public class DaoGistic {
    /**
     * Adds a ROI with Gistic info to the database
     *
     * @param  gistic            Gistic object
     * @throws SQLException
     * @throws DaoException
     */

    public static void addGistic(Gistic gistic) throws SQLException, DaoException {
        if (gistic == null) {
            throw new DaoException("Given a null gistic object");
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection();
            // insert into SQL gistic table
            pstmt = con.prepareStatement
                    ("INSERT INTO gistic (`CANCER_STUDY_ID`," +
                            "`CHROMOSOME`, " +
                            "`WIDE_PEAK_START`, " +
                            "`WIDE_PEAK_END`, " +
                            "`Q_VALUE`, "  +
                            "`RES_Q_VALUE`, " +
                            "`AMP_DEL`) "  +
                            "VALUES (?,?,?,?,?,?,?)");

            pstmt.setInt(1, gistic.getCancerStudyId());
            pstmt.setInt(2, gistic.getChromosome()) ;
            pstmt.setInt(3, gistic.getPeakStart());
            pstmt.setInt(4, gistic.getPeakEnd());
            pstmt.setDouble(5, gistic.getqValue());
            pstmt.setDouble(6, gistic.getRes_qValue());
            pstmt.setBoolean(7, gistic.getAmpDel());
            pstmt.executeUpdate();

            // insert into SQL gistic_to_gene table
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int autoId = rs.getInt(1);
                gistic.setInternalId(autoId);
            }
            addGisticGenes(gistic);

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Helper function for addGistic.  Adds the genes in the ROI to the database (gistic_to_genes)
     * @param gistic
     * @throws SQLException
     * @throws DaoException
     */
    public static void addGisticGenes(Gistic gistic) throws SQLException, DaoException {
        ArrayList<CanonicalGene> genes = gistic.getGenes_in_ROI();
        PreparedStatement pstmt = null;
        Connection con = null;

        try {
            con = JdbcUtil.getDbConnection();
            if (!genes.isEmpty()) {
                for (CanonicalGene g : genes) {
                    pstmt = con.prepareStatement
                            ("INSERT INTO gistic_to_gene (`GISTIC_ROI_ID`," +
                                    "`ENTREZ_GENE_ID`)" +
                                    "VALUES (?,?)");

                    pstmt.setInt(1, gistic.getInternalId());
                    pstmt.setLong(2, g.getEntrezGeneId());

                    pstmt.executeUpdate();
                }
            } else {
                throw new DaoException("No genes associated with given gistic");
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    /**
     *
     * Extracts Gistic JDBC Results.
     * @param rs Result Set of a JDBC database query
     * @return Gistic
     * @throws SQLException
     * @throws DaoException
     */
    private static Gistic extractGistic(ResultSet rs) throws SQLException, DaoException, validationException {

        // get the genes from the SQL gistic_to_gene table
        // associated with a particular GISTIC_ROI_ID
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet _rs = null;
        Gistic gistic;
        ArrayList<CanonicalGene> genes = new ArrayList<CanonicalGene>();
        int id = rs.getInt("GISTIC_ROI_ID");

        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("SELECT * FROM gistic_to_gene WHERE GISTIC_ROI_ID = ?");
            pstmt.setInt(1, id);

            _rs = pstmt.executeQuery();

            while ( _rs.next() ) {
                long entrez = _rs.getLong("ENTREZ_GENE_ID");

                CanonicalGene gene = DaoGeneOptimized.getInstance().getGene(entrez);
                // this may turn out to be a problem.
                // We may want to modify DaoOptimized.guessGene to ensure that it returns a single gene
                assert(gene != null);
                genes.add(gene);
            }

            // create gistic return object
            gistic = new Gistic(rs.getInt("CANCER_STUDY_ID"),
                    rs.getInt("CHROMOSOME") ,
                    rs.getInt("WIDE_PEAK_START"),
                    rs.getInt("WIDE_PEAK_END"),
                    rs.getDouble("Q_VALUE"),
                    rs.getDouble("RES_Q_VALUE") ,
                    genes,
                    rs.getBoolean("AMP_DEL"));

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, _rs);
        }

        ValidateGistic.validateBean(gistic);
        return gistic;
    }

    /**
     * Given an ROI, returns associated Gistic objects.
     * Right now, perhaps this is useless, but maybe something for the future?
     * @param chromosome
     * @param peakStart
     * @param peakEnd
     * @return
     * @throws DaoException
     */
    public static ArrayList<Gistic> getGisticByROI(int chromosome, int peakStart, int peakEnd) throws DaoException, validationException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("SELECT * FROM gistic WHERE CHROMOSOME = ? " +
                    "AND WIDE_PEAK_START = ? " +
                    "AND WIDE_PEAK_END = ?");

            pstmt.setInt(1, chromosome);
            pstmt.setInt(2, peakStart);
            pstmt.setInt(3, peakEnd);

            rs = pstmt.executeQuery();
            ArrayList<Gistic> list = new ArrayList<Gistic>();
            
            while( rs.next() ) {
                Gistic gistic = extractGistic(rs);
                list.add(gistic);
            }
            return list;

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Returns a list of all Gistic objects associated with a particular CancerStudy
     * @param cancerStudyId         CancerStudyId (of a database record)
     * @return
     */

    public static ArrayList<Gistic> getAllGisticByCancerStudyId(int cancerStudyId) throws DaoException, validationException {

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("SELECT * FROM gistic WHERE CANCER_STUDY_ID = ? ");
            pstmt.setInt(1, cancerStudyId);

            rs = pstmt.executeQuery();
            ArrayList<Gistic> list = new ArrayList<Gistic>();

            while( rs.next() ) {
                Gistic gistic = extractGistic(rs);
                list.add(gistic);
            }
            return list;

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes all Gistic records in the database (including gistic and gistic_to_gene tables)
     * @throws DaoException
     */

    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection();

            pstmt = con.prepareStatement("TRUNCATE TABLE gistic_to_gene");
            pstmt.executeUpdate();

            pstmt = con.prepareStatement("TRUNCATE TABLE gistic");
            pstmt.executeUpdate();


        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes a particular Gistic record in the database (including corresponding gistic_to_gene records)
     * @param gisticInternalId
     * @throws DaoException
     */

    public static void deleteGistic(int gisticInternalId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection();

            pstmt = con.prepareStatement("DELETE from gistic_to_gene WHERE GISTIC_ROI_ID=?");
            pstmt.setInt(1, gisticInternalId);
            pstmt.executeUpdate();

            pstmt = con.prepareStatement("DELETE from gistic WHERE GISTIC_ROI_ID=?");
            pstmt.setInt(1, gisticInternalId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}
