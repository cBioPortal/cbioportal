package org.mskcc.cgds.util;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
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
     * Loads a gistic with parameters: chromosome, peak_start, peak_end, and genes_in_peak
     * Leaves dummy variables in for fields to be filled in by other methods.
     *
     * @param gisticFile        gistic data file (txt)
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ArrayList<Gistic> parse_Table(File gisticFile) throws FileNotFoundException, IOException, DaoException {
        ArrayList<Gistic> gistics = new ArrayList<Gistic>();

        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(gisticFile);
        BufferedReader buf = new BufferedReader(reader);

        String line = buf.readLine();

        // -- parse field names --
        // would it be better to use <enums>?
        int chromosomeField = -1;
        int peakStartField = -1;
        int peakEndField = -1;
        int genesField = -1;

        String[] fields = line.split("\t");
        int num_fields = fields.length;

        for (int i = 0 ; i < num_fields; i+=1) {

            if (fields[i].equals("chromosome")) {
                chromosomeField = i;
            }

            else if (fields[i].equals("peak_start")) {
                peakStartField = i;
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
                    || fields[i].equals("index"))   { continue; }       // ignore these fields

            else {
                throw new IOException("bad file format.  Field: " + fields[i] + " not found");
            }
        }

        assert(chromosomeField != -1);
        assert(peakStartField != -1);
        assert(peakEndField != -1);
        assert(genesField != -1);
        // -- end parse field names --

        // parse file
        line = buf.readLine();
        while (line != null) {

            fields = line.split("\t");

            Gistic gistic = new Gistic();

            gistic.setChromosome(Integer.parseInt(fields[chromosomeField]));
            gistic.setPeakStart(Integer.parseInt(fields[peakStartField]));
            gistic.setPeakEnd(Integer.parseInt(fields[peakEndField]));

            // -- parse genes --

            // parse out '[' and ']' chars and,         ** Do these brackets have meaning? **
            // split
            String[] _genes = fields[genesField].replace("[","")
                    .replace("]", "")
                    .split(",");

            // map _genes to list of CanonicalGenes
            ArrayList<CanonicalGene> genes = new ArrayList<CanonicalGene>();
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
            for (String gene : _genes) {
                CanonicalGene canonicalGene = daoGene.getNonAmbiguousGene(gene);

                if (canonicalGene == null) {
                    throw new DaoException("Canonical Gene not found for: " + gene);
                }

                genes.add(canonicalGene);
            }
            // -- end parse genes
            gistic.setGenes_in_ROI(genes);

            gistics.add(gistic);
            line = buf.readLine();
        }
        return gistics;
    }

    /**
     * Parses files of the form amp_genes.conf_99.txt
     * i.e. files without the word table in them
     * @param gisticFile
     * @return
     */
    public Gistic[] parseNonTabular(File gisticFile) throws FileNotFoundException, IOException, DaoException {
        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(gisticFile);
        BufferedReader buf = new BufferedReader(reader);

        /**
         *
         * read each line of the file,
         * determine which feature that line (row) is representing,
         * fill the gistics accordingly
         *
         * note: we parse the genes so that we have
         * something to map the gistics parsed here to gistics parsed elsewhere
         */
        String line = buf.readLine();
        int example_no = line.split("\t").length;    // todo:is there a better way?
        Gistic[] gistics = new Gistic[example_no - 1];

        // fill gistics will dummy gistics
        for (int i = 0; i < example_no - 1; i+=1) {
            gistics[i] = new Gistic();
        }

        String[] split;
        while (line != null) {
            split = line.split("\t");

            // sometimes a trailing tab is omitted,
            // but anything more than that should be caught
            if ((split.length < example_no - 1) || (split.length > example_no + 1)) {
                throw new IOException(String.format("Number of features is not the same for all examples " +
                        "(assumed no_of_examples=%d, but given %d)", example_no, split.length));
            }

//            System.out.println("-- nextline --");
//            System.out.println("gistics.length=" + gistics.length);
//            System.out.println("split.length=" + split.length);
//            System.out.println("example_no=" + example_no);
//            System.out.println(split[0]);

            if (split[0].equals("q value")) {
                for (int i = 0; i < example_no - 1; i += 1) {       // i = 0 is the name of the feature
//                    System.out.println(String.format("gistics[%d] ",i) + gistics[i]);
                    gistics[i].setqValue(Float.valueOf(split[i + 1]));
//                    System.out.println(String.format("gistics[%d] ",i) + gistics[i]);
                }
            }

            else if (split[0].equals("residual q value")) {
                for (int i = 1; i < example_no - 1; i += 1) {
                    gistics[i].setRes_qValue(Float.valueOf(split[i]));
                }
            }

            else if (split[0].equals("genes in wide peak") || split[0].equals("")) {
                /** assuming that if the place where there is normally a field instead contains
                 * the empty string, then it actually contains a gene
                 * this is because genes are separated by newline characters instead of by commas.
                 */
                for (int i = 1; i < example_no - 1; i += 1) {       // i = 0 is the name of the feature

                    // empty string
                    if (split[i].length() == 0) { continue; }
                    
                    // parse out '[' and ']'
                    String _gene = split[i].replace("[", "").replace("]","");
                    
                    // get the Canonical Gene
                    CanonicalGene gene = DaoGeneOptimized.getInstance().getNonAmbiguousGene(_gene);

                    if (gene == null) {
                        throw new DaoException("Canonical Gene not found for: " + gene);
                    }
                    
                    gistics[i].addGene(gene);
                }
            }

            else if (split[0].equals("cytoband")
                    || split[0].equals("wide peak boundaries")) { line = buf.readLine(); continue; }       // ignore these fields

            else {
                throw new IOException("bad file format.  Field: " + split[0] + " not found");
            }

            line = buf.readLine();
        }

        return gistics;
    }

    /**
     * Merges two orthogonal gistics together
     * The gistics data is located in two separate files which are parsed separately.
     * This method merges the gistic objects parsed from the two files.
     *
     * @param g1
     * @param g2
     * @return
     * @throws Exception
     */
    //todo: Obvious downside, twice as many gistic objects are created than what is needed.
    public Gistic[] mergeGistics(ArrayList<Gistic> g1, Gistic[] g2) throws Exception {
        
        int g1_len = g1.size();
        int g2_len = g2.length;
        
        if (g1_len != g2_len) {
            throw new Exception(String.format("Cannot merge Gistic arrays of different sizes: %d, %d", g1_len, g2_len));
        }
        
        Gistic[] g1_array = new Gistic[g1.size()];
        g1.toArray(g1_array);

        /**
         * For each gistic in g1, find its partner in g2 by common gene set
         */
        for (int i = 0; i < g1_len; i+=1) {

            ArrayList<CanonicalGene> g1_genes = g1_array[i].getGenes_in_ROI();
            CanonicalGene gene1 = g1_genes.get(0);
            int no_genes_g1 = g1_genes.size();

            // if they have the same number of genes and the their first gene is the same
            // it's highly likely that they are a match
           for (int j = 0; j < g2_len; i+=1) { // g1_len == g2_len
               ArrayList<CanonicalGene> g2_genes = g1_array[i].getGenes_in_ROI();
               CanonicalGene gene2 = g2_genes.get(0);
               int no_genes_g2 = g2_genes.size();

               if (gene1.equals(gene2) && no_genes_g1 == no_genes_g2) {
                   // todo: merge g1 and g2
               }

               else {
                   continue;
               }
           }
            // todo: check that all fields are filled
        }
    }
//        DaoGistic.addGistic(gistic);
}


