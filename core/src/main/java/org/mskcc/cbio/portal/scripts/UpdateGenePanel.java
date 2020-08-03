package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.io.*;
import java.util.*;

import joptsimple.*;

import static org.mskcc.cbio.portal.util.GenePanelUtil.extractGenes;
import static org.mskcc.cbio.portal.util.GenePanelUtil.extractPropertyValue;

/**
 * Updates an existing, potentially in use gene panel.
 *
 * @author jtquach1
 */
public class UpdateGenePanel extends ConsoleRunnable {

    private File genePanelFile;
    private static final Scanner s = new Scanner(System.in);

    @Override
    public void run() {
        try {
            String progName = "UpdateGenePanel";
            String description = "Update gene panel files.";

            OptionParser parser = new OptionParser();
            OptionSpec<String> data = parser
                .accepts("data", "gene panel file")
                .withRequiredArg()
                .describedAs("data_file.txt")
                .ofType(String.class);
            parser.accepts("noprogress",
                "this option can be given to avoid the messages regarding memory usage and % complete");

            OptionSet options;
            try {
                options = parser.parse(args);
            } catch (OptionException e) {
                throw new UsageException(progName, description, parser, e.getMessage());
            }
            File genePanel_f;
            if (options.has(data)) {
                genePanel_f = new File(options.valueOf(data));
            } else {
                throw new UsageException(progName, description, parser, "'data' argument required.");
            }

            setFile(genePanel_f);
            importData();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void importData() throws Exception {
        ProgressMonitor.setCurrentMessage("Reading data from:  " + genePanelFile.getAbsolutePath());
        Properties properties = new Properties();
        properties.load(new FileInputStream(genePanelFile));

        ProgressMonitor.setCurrentMessage("Retrieving matching gene panel from database.");
        String stableId = extractPropertyValue("stable_id", properties, true);
        Set<CanonicalGene> canonicalGenes = extractGenes(properties, true);
        GenePanel genePanel = DaoGenePanel.getGenePanelByStableId(stableId);

        if (genePanel == null) {
            ProgressMonitor.logWarning("Gene panel " + stableId + " does not exist in the database! Exiting.");
            return;
        }

        if (canonicalGenes == null || canonicalGenes.isEmpty()) {
            ProgressMonitor.logWarning("Incoming gene panel is empty, which would result in the removal of all genes from gene panel " + stableId + ". Exiting.");
            return;
        }

        System.err.println("WARNING: You are about to make the following changes to gene panel " + stableId + ":");
        DaoGenePanel.updatePreview(genePanel, canonicalGenes);
        System.err.println("Proceed? (Y/n)");

        if (confirmContinue()) {
            if (DaoSampleProfile.sampleProfileMappingExistsByPanel(genePanel.getInternalId())) {
                ProgressMonitor.setCurrentMessage("Gene panel " + stableId
                    + " already exists in database and is being used. Proceeding with gene panel update!");
            } else {
                ProgressMonitor.setCurrentMessage("Gene panel " + stableId
                    + " already exists in database but is unused. Proceeding with gene panel update!");
            }
            DaoGenePanel.updateGenePanel(genePanel, canonicalGenes);
        }
    }

    private static boolean confirmContinue() {
        boolean confirmed = false;
        String answer = s.nextLine().toUpperCase();

        switch (answer) {
            case "Y":
                System.err.println("Proceeding with update script.");
                confirmed = true;
                break;
            case "N":
                System.err.println("Exiting update script.");
                break;
            default:
                System.err.println("Unrecognized input, exiting.");
                break;
        }

        return confirmed;
    }

    public void setFile(File genePanelFile) {
        this.genePanelFile = genePanelFile;
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args the command line arguments to be used
     */
    public UpdateGenePanel(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new UpdateGenePanel(args);
        System.out.print(
            "WARNING: This script is ONLY for updating a gene panel, not for adding a new gene panel.\n" +
                "To add a new gene panel, run the ImportGenePanel script.\n" +
                "If you are making changes to an existing gene panel due to a miscount of genes, proceed.\n" +
                "Otherwise, if you would like to add a new version of a gene panel, please exit this script\n" +
                "and import the gene panel with a new stable ID!\n" +
                "Will you proceed to update an existing gene panel? (Y/n)\n"
        );

        if (confirmContinue()) {
            runner.runInConsole();
            s.close();
        }
    }
}
