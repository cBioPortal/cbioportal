package org.mskcc.cgds.util;

import org.junit.experimental.theories.internal.ParameterizedAssertionError;
import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.Gistic;
import org.mskcc.cgds.validate.ValidateGistic;
import org.mskcc.cgds.validate.validationException;
import sun.security.validator.ValidatorException;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
    public int getCancerStudyInternalId(File cancerStudyMeta)
            throws DaoException, IOException, FileNotFoundException  {

        Properties properties = new Properties();
        properties.load(new FileInputStream(cancerStudyMeta));

        String cancerStudyIdentifier = properties.getProperty("cancer_study_identifier");

        if (cancerStudyIdentifier == null) {
            throw new IllegalArgumentException("cancer_study_identifier is not specified.");
        }

        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);

        if (cancerStudy == null) {
            throw new DaoException(cancerStudyIdentifier);
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
    public ArrayList<Gistic> parse_Table(File gisticFile) throws FileNotFoundException, IOException, DaoException, validationException {
        ArrayList<Gistic> gistics = new ArrayList<Gistic>();

        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(gisticFile);
        BufferedReader buf = new BufferedReader(reader);

        String line = buf.readLine();

        // -- parse field names --
        // todo: would it be better to use <enums>?
        int chromosomeField = -1;
        int peakStartField = -1;
        int peakEndField = -1;
        int genesField = -1;

        String[] fields = line.split("\t");
        int num_fields = fields.length;

        ValidateGistic.validateFieldNames_tabularFile(fields);

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

            else if (fields[i].equals("genes_in_region")) {
                genesField = i;
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
                    throw new DaoException(gene);
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
     * Loads Gistics from a file where the first field of the filename is table,
     * e.g. amp_genes.conf_99.txt
     *
     * Loads a gistic with parameters: p-value, residual p-value,
     * and genes in wide peak (to be used as a key-map to gistics parsed by other methods from other files)
     * Leaves dummy variables in for fields to be filled in by other methods.
     * @param gisticFile    Gistic data file
     * @return
     */
    public ArrayList<Gistic> parse_NonTabular(File gisticFile) throws FileNotFoundException, IOException, DaoException, validationException {
        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(gisticFile);
        BufferedReader buf = new BufferedReader(reader);

        /**
         * read each line of the file,
         * determine which feature that line (row) is representing,
         * fill the gistics accordingly
         *
         * note: we parse the genes in order to map between
         * gistics parsed here and gistics parsed elsewhere
         */
        String line = buf.readLine();
        int example_no = line.split("\t").length;    // todo:is there a better way?
        ArrayList<Gistic> gistics = new ArrayList<Gistic>(example_no - 1);
        
        // fill gistics will dummy gistics
        for (int i = 0; i < example_no - 1; i+=1) {
            gistics.add(new Gistic());
        }

        String[] split;
        while (line != null) {
            split = line.split("\t");

            ValidateGistic.validateNonTabularRow(split);

            if (split[0].equals("q value")) {
                for (int i = 0; i < example_no - 1; i += 1) {       // i = 0 is the name of the feature
                    gistics.get(i).setqValue(Double.valueOf(split[i + 1]));
                }
            }

            else if (split[0].equals("residual q value")) {
                for (int i = 0; i < example_no - 1; i += 1) {
                    gistics.get(i).setRes_qValue(Double.valueOf(split[i + 1]));
                }
            }

            else if (split[0].equals("genes in wide peak") || split[0].equals("")) {

            // genes are stuck on at the ends of the file
            // so the number of fields of a gene line may vary
            int no_fields = split.length;

                for (int i = 0; i < no_fields - 1; i += 1) {

                    String gene_str = split[i + 1];

                    // empty string
                    if (gene_str.length() == 0) { continue; }

                    // parse out '[' and ']'
                    gene_str = gene_str.replace("[", "").replace("]","");

                    // get the Canonical Gene
                    CanonicalGene gene = DaoGeneOptimized.getInstance().getNonAmbiguousGene(gene_str);

                    if (gene == null) {
                        throw new DaoException(gene.toString());
                    }

                    gistics.get(i).addGene(gene);
                }
            }

            line = buf.readLine();
        }
        
        return gistics;
    }

    /**
     * Merges the fields from two gistics.  Specifically, merges:
     * q-value, residual q-value, chromosome, peak_start, and peak_end
     *
     * Note: Does *not* check that gistics are a match
     * @param g1 Gistic object
     * @param g2 Gistic object
     * @return Gistic object
     */
    public Gistic mergeGistics(Gistic g1, Gistic g2) throws IOException {
        CanonicalGene g1_first_gene = g1.getGenes_in_ROI().get(0);
        CanonicalGene g2_first_gene = g2.getGenes_in_ROI().get(0);
        
        int no_genes_g1 = g1.getGenes_in_ROI().size();
        int no_genes_g2 = g2.getGenes_in_ROI().size();
        
        Gistic dummyGistic = new Gistic();

            // merges into g1
            // merge q-value
            if (g1.getqValue() == dummyGistic.getqValue()) {

                assert(g2.getqValue() != dummyGistic.getqValue());
                g1.setqValue(g2.getqValue());
            }

            // merge residual q-value
            if (g1.getRes_qValue() == dummyGistic.getRes_qValue()) {

                assert(g2.getRes_qValue() != dummyGistic.getRes_qValue());
                g1.setRes_qValue(g2.getRes_qValue());
            }

            // merge chromosome into g1
            if (g1.getChromosome() == dummyGistic.getqValue()) {

                assert(g2.getChromosome() != dummyGistic.getqValue());
                g1.setChromosome(g2.getChromosome());
            }

            // merge peak_start into g1
            if (g1.getPeakStart() == dummyGistic.getPeakStart()) {

                assert(g2.getPeakStart() != dummyGistic.getPeakStart());
                g1.setPeakStart(g2.getPeakStart());
            }

            // merge peak_end into g1
            if (g1.getPeakEnd() == dummyGistic.getPeakEnd()) {

                assert(g2.getPeakEnd() != dummyGistic.getPeakEnd());
                g1.setPeakEnd(g2.getPeakEnd());
            }

        return g1;
    }
        
    //todo: Obvious downside, twice as many gistic objects are created than what is needed.
    /**
     * Merges two orthogonal gistics together.  Specifically, merge their:
     * q-value, residual q-value, chromosome, peak_start, and peak_end)
     *
     * The gistics data is located in two separate files which are parsed separately.
     * This method merges the gistic objects parsed from the two files.
     *
     * @param g1 ArrayList of gistics
     * @param g2 ArrayList of gistics
     * @return
     * @throws Exception
     */
    public ArrayList<Gistic> mergeGisticLists(ArrayList<Gistic> g1, ArrayList<Gistic> g2, boolean ampdel) throws IOException, validationException {
        
        int g1_len = g1.size();
        int g2_len = g2.size();

        if (g1_len != g2_len) {
            throw new IOException(String.format("Cannot merge Gistic arrays of different sizes: %d, %d", g1_len, g2_len));
        }

        Gistic dummyGistic = new Gistic();  // used for comparison to figure out which fields are dummy

        // iterate over g1
        for (int i = 0; i < g1_len; i+=1) {     //todo: change this loop
            // g1 objects for comparison
            ArrayList<CanonicalGene> g1_genes = g1.get(i).getGenes_in_ROI();
            CanonicalGene g1_first_gene = g1_genes.get(0);
            int no_genes_g1 = g1_genes.size();

            Gistic g1_i = g1.get(i);

            // look for a gistic match in g2
            for (int j = 0; j < g2_len; j+=1) { // g1_len == g2_len
                // g2 objects for comparison
                ArrayList<CanonicalGene> g2_genes = g2.get(j).getGenes_in_ROI();
                CanonicalGene g2_first_gene = g2_genes.get(0);
                int no_genes_g2 = g2_genes.size();

                // if they have the same number of genes and their first gene is the same,
                // then they are (very) likely to be a match
                if (g1_first_gene.equals(g2_first_gene) && no_genes_g1 == no_genes_g2) {
                    Gistic merged_gistic = mergeGistics(g1.get(i), g2.get(j));
                    merged_gistic.setAmpDel(ampdel);

                    // merge into g1
                    g1.set(i, merged_gistic);
                }
            }

            ValidateGistic.validateBean(g1_i);
        }
        return g1;
    }
    
    public boolean parseAmpDel(File gistic_file) throws IOException, validationException {

        boolean amp = gistic_file.getName().indexOf("amp") != -1 ? true : false;    // likely to be Amplified ROI
        boolean del = gistic_file.getName().indexOf("del") != -1 ? true : false;    // likely to be Deleted ROI
        
        if (amp && del) {
            throw new validationException("Amp == Del");
        }

        return amp ? Gistic.AMPLIFIED : Gistic.DELETED;
    }

    public void loadGistic(int cancerStudyInternalId, File table_file, File nontable_file, boolean ampdel) throws IOException, DaoException, SQLException, validationException {
        
        ArrayList<Gistic> gistics;

        if (ampdel == Gistic.AMPLIFIED) {     // table_ampdel == nontable_ampdel
            gistics = mergeGisticLists(parse_Table(table_file),
                    parse_NonTabular(nontable_file),
                    Gistic.AMPLIFIED);
        }

        else if (ampdel == Gistic.DELETED) {
            gistics = mergeGisticLists(parse_Table(table_file),
                    parse_NonTabular(nontable_file),
                    Gistic.DELETED);
        }

        else {
            throw new validationException("");
        }

        // set the Cancer Study internal ID
        // and add to CGDS database
        for (Gistic g : gistics) {
            g.setCancerStudyId(cancerStudyInternalId);

//            System.out.println(g);
            DaoGistic.addGistic(g);
        }
    }
}
