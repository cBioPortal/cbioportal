package org.mskcc.cbio.cgds.dao;

import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.Gistic;
import org.mskcc.cbio.cgds.validate.ValidateGistic;
import org.mskcc.cbio.cgds.validate.validationException;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
     * @throws DaoException
     */

    private static Log log = LogFactory.getLog(DaoGistic.class);

    public static void addGistic(Gistic gistic) throws DaoException {
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
                  "`CYTOBAND`, " +
				  "`WIDE_PEAK_START`, " +
				  "`WIDE_PEAK_END`, " +
				  "`Q_VALUE`, "  +
				  "`AMP`) "  +
				  "VALUES (?,?,?,?,?,?,?)",
				 Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1, gistic.getCancerStudyId());
            pstmt.setInt(2, gistic.getChromosome()) ;
            pstmt.setString(3, gistic.getCytoband());
            pstmt.setInt(4, gistic.getPeakStart());
            pstmt.setInt(5, gistic.getPeakEnd());
            pstmt.setDouble(6, gistic.getqValue());
            pstmt.setBoolean(7, gistic.getAmp());
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

                    // EntrezId = -1 if it does not exist in the gene table
                    // if this is the case, we are going to simply skip over this gene
                    if (g.getEntrezGeneId() != -1) {
                    pstmt = con.prepareStatement
                            ("INSERT INTO gistic_to_gene (`GISTIC_ROI_ID`," +
                                    "`ENTREZ_GENE_ID`)" +
                                    "VALUES (?,?)");

                    pstmt.setInt(1, gistic.getInternalId());
                    pstmt.setLong(2, g.getEntrezGeneId());

                    pstmt.executeUpdate();
                    }

                    else {
                        throw new DaoException("gene not found, skipping: " + g);
                    }
                }
            } else {
                throw new DaoException("No genes associated with given gistic");
            }

        } catch (SQLException e) {

            if (log.isDebugEnabled()) {
                log.debug(e + " : " + genes);
            }

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
    private static Gistic extractGistic(ResultSet rs) throws DaoException, validationException {

        // get the genes from the SQL gistic_to_gene table
        // associated with a particular GISTIC_ROI_ID
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet _rs = null;
        Gistic gistic;
        ArrayList<CanonicalGene> genes = new ArrayList<CanonicalGene>();

        try {

            int id = rs.getInt("GISTIC_ROI_ID");

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
                    rs.getString("CYTOBAND") ,
                    rs.getInt("WIDE_PEAK_START"),
                    rs.getInt("WIDE_PEAK_END"),
                    rs.getFloat("Q_VALUE") ,
                    genes,
                    rs.getBoolean("AMP"));

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
     * Returns the number of rows in the gistic database table
     * @param cancerStudy cancerStudyId
     * @return no. of gistics
     * @throws DaoException
     */
    public static int countGistic(int cancerStudy) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT count(*) FROM gistic WHERE CANCER_STUDY_ID = ?");
            pstmt.setInt(1, cancerStudy);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;

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

    /**
     * Asks whether the gistic database table is empty.
     * @param cancerStudy
     * @return True is there are gistics for cancerStudy, false if there are not.
     * @throws DaoException
     */
    public static boolean hasGistic(CancerStudy cancerStudy) throws DaoException {
        return !(countGistic(cancerStudy.getInternalId()) == 0);
    }

    /**
     * Returns all gistics in the database
     * @return ArrayList of gistics
     * @throws DaoException
     * @throws validationException
     */
    public static ArrayList<Gistic> getAllGistic() throws DaoException, validationException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("SELECT * FROM gistic");

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
}
