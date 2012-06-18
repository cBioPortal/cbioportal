package org.mskcc.cgds.util;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.MySQLbulkLoader;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.Gistic;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Utility for importing Gistic data from a file
 */
public class GisticReader {

    /**
     * Extracts find the database's internal Id for the record
     * associated with the Cancer Study described the metafile
     * @param cancerStudyMeta   File
     * @return                  CancerStudyId
     * @throws DaoException
     * @throws IOException
     */
    public static int getCancerStudyInternalId(File cancerStudyMeta)
            throws DaoException, IOException, FileNotFoundException  {

            Properties properties = new Properties();
            properties.load(new FileInputStream(cancerStudyMeta));

            String cancerStudyIdentifier = properties.getProperty("cancer_study_identifier");

            if (cancerStudyIdentifier == null) {
                throw new IllegalArgumentException("cancer_study_identifier is not specified.");
            }

            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);

            if (cancerStudy == null) {
                throw new DaoException("no CancerStudy associated with \""
                        + cancerStudyIdentifier + "\" cancer_study_identifier");
            }

            return cancerStudy.getInternalId();
        }

    /**
     * Loads Gistics from a file and into the database
     * @param internalId        Cancer Study Internal Id
     * @param ampDel            is ROI a region of amplification or of deletion
     * @param gisticFile        gistic data file (txt)
     * @param pMonitor          Progress Monitor
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void loadGistic(int internalId, boolean ampDel, File gisticFile, ProgressMonitor pMonitor) throws FileNotFoundException, IOException {
        Gistic gistic = null;

        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(gisticFile);
        BufferedReader buf = new BufferedReader(reader);

        String line = buf.readLine();
        int columnNum = line.split("\t").length;
        
        Gistic[] gistics;
        gistics = new Gistic[columnNum - 1];
        
        Gistic placeHolder = new Gistic(-1, "-1", -1, -1, "-1", "-1", new ArrayList<CanonicalGene>(), false);

        // fill the gisitics array with placeholders
        for (int i = 0; i < columnNum - 1; i += 1 ) { // the first column is field names
            gistics[i] = placeHolder;
        }

        // iterate through the rows
        // filling fields of the gistics as you go
        String[] columns;
        
        System.out.println(line.split("\t")[0]);
        line = buf.readLine();

        System.out.println(line.split("\t")[0]);
        line = buf.readLine();

        System.out.println(line.split("\t")[0]);
        line = buf.readLine();

        System.out.println(line.split("\t")[0]);
        line = buf.readLine();

        System.out.println(line.split("\t")[0]);
        line = buf.readLine();

        while (line != null)
        {
            columns = line.split("\t");

            if (columns[0] == "cytoband") {
                for (int i = 1; i < columns.length; i += 1) {
                    gistics[i-1].setCytoband(columns[i]);
                }
            }

            if (columns[0] == "q value") {
                for (int i = 1; i < columns.length; i += 1) {
                    gistics[i-1].setqValue(columns[i]);
                }
            }

            if (columns[0] == "residual q value") {
                for (int i = 1; i < columns.length; i += 1) {
                gistics[i-1].setRes_qValue(columns[i]);
                }
            }

            if (columns[0] == "wide peak boundaries") {
                String[] peakLoci;
                peakLoci = new String[2];
                
                for (int i = 1; i < columns.length; i += 1) {
                    peakLoci = columns[i].split(":")[1].
                            split("-");
                    gistics[i-1].setPeakStart(Integer.parseInt(peakLoci[0]));
                    gistics[i-1].setPeakEnd(Integer.parseInt(peakLoci[1]));
                }
            }

            if (columns[0] == "genes in wide peak") {
                ArrayList<CanonicalGene> genes = null;
                for (int i = 1; i < columns.length; i += 1) {
//                    genes = columns[i].split(" ")
                    columns[i].split(" ");
                    System.out.println("NumofGenes " + columns[i].split(" ").length);
//                    gistics[i-1].setGenes_in_ROI(genes);
                }
            }

            line = buf.readLine();
        }


//        DaoGistic.addGistic(gistic);

    }
}
