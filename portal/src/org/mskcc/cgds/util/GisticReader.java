package org.mskcc.cgds.util;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.MySQLbulkLoader;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.Gistic;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Utility for importing Gistic data from a file
 */
public class GisticReader {

    Gistic dummyGistic = new Gistic();  // used to pass around partially completed gistic objects

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
     * Loads Gistics from a file where the first field of the filename is table,
     * e.g. table_amp.conf_99.txt
     *
     * Fills in dummy variables for fields to be filled in by other methods.
     *
     * @param gisticFile        gistic data file (txt)
     * @param pMonitor          Progress Monitor
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ArrayList<Gistic> parse_Table(File gisticFile, ProgressMonitor pMonitor) throws FileNotFoundException, IOException {
        ArrayList<Gistic> gistics = new ArrayList<Gistic>();

        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(gisticFile);
        BufferedReader buf = new BufferedReader(reader);

        String line = buf.readLine();

        // parse field names
        int chromosomeField = -1;
        int peakStartField = -1;
        int peakEndField = -1;
        int genesField = -1;

        String[] fields = line.split("\t");
        int num_fields = fields.length;

        for (int i = 0 ; i < num_fields; i+=1) {

            if (fields[i].equals("chromosome")) {
                peakStartField = i;
            }

            else if (fields[i].equals("peak_start")) {
                chromosomeField = i;
            }

            else if (fields[i].equals("peak_end")) {
                peakEndField = i;
            }

            else if (fields[i].equals("genes_in_peak")) {
                genesField = i;
            }

            else if (fields[i].equals("genes_in_region")
                    || fields[i].equals("n_genes_on_chip")
                    || fields[i].equals("genes_on_chip")
                    || fields[i].equals("top 3")
                    || fields[i].equals("n_genes_in_region")
                    || fields[i].equals("n_genes_in_peak")
                    || fields[i].equals("region_start")
                    || fields[i].equals("region_end")
                    || fields[i].equals("enlarged_peak_start")
                    || fields[i].equals("enlarged_peak_end")
                    || fields[i].equals("index"))  { continue; }       // ignore these fields

            else {
                throw new IOException("bad file format.  Field: " + fields[i] + " not found");
            }
        }

        assert(chromosomeField != -1);
        assert(peakStartField != -1);
        assert(peakEndField != -1);
        assert(genesField != -1);
        // end parse field names

        // parse file
        while (line != null) {

            fields = line.split("\n");

            Gistic gistic = new Gistic();

            gistic.setChromosome(Integer.getInteger(fields[chromosomeField]));
            gistic.setPeakStart(Integer.getInteger(fields[peakStartField])) ;
            gistic.setPeakEnd(Integer.getInteger(fields[peakEndField])) ;


            // parse out the genes
            // ...

            System.out.println("genes " + fields[genesField]);

            gistics.add(gistic);
            line = buf.readLine();
        }

        return gistics;
    }

    public ArrayList<Gistic> parse_qValue(File gisticFile) {
        ArrayList<Gistic> gistics = new ArrayList<Gistic>();

        return gistics;
    }


//        DaoGistic.addGistic(gistic);

}
