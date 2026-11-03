/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoReferenceGenome;
import org.mskcc.cbio.portal.model.ReferenceGenome;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 * Command Line Tool to Import Reference Genome Used by Molecular Profiling.
 */
public class ImportReferenceGenome extends ConsoleRunnable {

    /**
     * Adds the genes parsed from the file into the Database.
     * @param referenceGenomeFile File with reference genome information
     * @throws IOException
     * @throws DaoException
     */
    public static void importData(File referenceGenomeFile) throws IOException, DaoException, ParseException {

        try (FileReader reader = new FileReader(referenceGenomeFile)) {
            BufferedReader buf = new BufferedReader(reader);
            String line;
            Set<ReferenceGenome> referenceGenomes = new HashSet<ReferenceGenome>();
            while ((line = buf.readLine()) != null) {
                ProgressMonitor.incrementCurValue();
                ConsoleUtil.showProgress();
                if (line.startsWith("#")) {
                    continue;
                }
                String parts[] = line.split("\t");
                String species = parts[0];
                String name = parts[1];
                String buildName = parts[2];
                String genomeSize = parts[3];
                String url = parts[4];
                String releaseDate = parts[5];

                ReferenceGenome referenceGenome = new ReferenceGenome(name, species, buildName);
                if (StringUtils.isNotEmpty(url)) {
                    referenceGenome.setUrl(url);
                }

                if (StringUtils.isNotEmpty(genomeSize)) {
                    referenceGenome.setGenomeSize(Long.parseLong(genomeSize));
                }

                if (StringUtils.isNotEmpty(releaseDate)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
                    referenceGenome.setReleaseDate(sdf.parse(releaseDate));

                }
                referenceGenomes.add(referenceGenome);
            }
            addReferenceGenomesToDB(referenceGenomes);
        }
    }

    /**
     * Iterate over the genes found in the given maps and try to add them to the DB.
     * @param referenceGenomes: reference genomes
     * @throws DaoException
     */
    private static void addReferenceGenomesToDB(Set<ReferenceGenome> referenceGenomes) throws DaoException {
        int nrExisting = 0;
        for (ReferenceGenome refGenome: referenceGenomes) {
            if (DaoReferenceGenome.getReferenceGenomeByInternalId(refGenome.getReferenceGenomeId()) != null) {
                ProgressMonitor.logWarning("Reference genome updated");
                try {
                    DaoReferenceGenome.updateReferenceGenome(refGenome);
                } catch (DaoException e) {
                    ProgressMonitor.logWarning("No change for " + refGenome.getGenomeName());
                }
            } else {
                ProgressMonitor.logWarning("New reference genome added");
                DaoReferenceGenome.addReferenceGenome(refGenome);
            }
        }
    }
    
    @Override
    public void run() {
        try {
            String description = "Update reference_genome table ";

            // using a real options parser, helps avoid bugs
            OptionParser parser = new OptionParser();
            OptionSpec<Void> help = parser.accepts( "help", "print this help info" );
            parser.accepts( "ref-genome", "reference genome file" ).withRequiredArg().describedAs("reference_genomes.txt").ofType( String.class );

            String progName = "importReferenceGenomes";
            OptionSet options = null;
            try {
                options = parser.parse( args );
            } catch (OptionException e) {
                throw new UsageException(progName, description, parser,
                    e.getMessage());
            }

            if( options.has( help ) ){
                throw new UsageException(progName, description, parser);
            }

            ProgressMonitor.setConsoleMode(true);

            File referenceGenomeFile;
            int numLines;
            if(options.has("ref-genome")) {
                File referenceFile = new File((String) options.valueOf("ref-genome"));

                System.out.println("Reading reference genome from:  " + referenceFile.getAbsolutePath());
                numLines = FileUtil.getNumLines(referenceFile);
                System.out.println(" --> total number of lines:  " + numLines);
                ProgressMonitor.setMaxValue(numLines);
                ClickHouseBulkLoader.bulkLoadOn();
                ImportReferenceGenome.importData(referenceFile);
            }

            ClickHouseBulkLoader.flushAll();
            System.err.println("Done. Restart tomcat to make sure the cache is replaced with the new data.");

        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Makes an instance to run with the given command line arguments.
     * @param args  the command line arguments to be used
     */
    public ImportReferenceGenome(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportReferenceGenome(args);
        runner.runInConsole();
    }

}
