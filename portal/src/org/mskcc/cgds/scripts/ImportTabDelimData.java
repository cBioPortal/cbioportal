package org.mskcc.cgds.scripts;

import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.ProgressMonitor;
import org.apache.commons.lang.ArrayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Code to Import Copy Number Alteration or MRNA Expression Data.
 *
 * @author Ethan Cerami
 */
public class ImportTabDelimData {
    private HashSet<Long> importedGeneSet = new HashSet<Long>();
    private HashSet<String> importedMicroRNASet = new HashSet<String>();
    
    /**
     * Barry Target Line:  A constant currently used to indicate the RAE method.
     */
    public final static String BARRY_TARGET = "Barry";

    /**
     * Consensus Target Line:  A constant currently used to indicate consensus of multiple
     * CNA calling algorithms.
     */
    public final static String CONSENSUS_TARGET = "consensus";

    private ProgressMonitor pMonitor;
    private File mutationFile;
    private String targetLine;
    private int geneticProfileId;
    private GeneticProfile geneticProfile;
    private HashSet<String> microRnaIdSet;

    /**
     * Constructor.
     *
     * @param dataFile         Data File containing CNA data.
     * @param targetLine       The line we want to import.
     *                         If null, all lines are imported.
     * @param geneticProfileId GeneticProfile ID.
     * @param pMonitor         Progress Monitor Object.
     */
    public ImportTabDelimData(File dataFile, String targetLine, int geneticProfileId,
            ProgressMonitor pMonitor) {
        this.mutationFile = dataFile;
        this.targetLine = targetLine;
        this.geneticProfileId = geneticProfileId;
        this.pMonitor = pMonitor;
    }

    /**
     * Constructor.
     *
     * @param dataFile         Data File containing CNA data.
     * @param geneticProfileId GeneticProfile ID.
     * @param pMonitor         Progress Monitor Object.
     */
    public ImportTabDelimData(File dataFile, int geneticProfileId, ProgressMonitor pMonitor) {
        this.mutationFile = dataFile;
        this.geneticProfileId = geneticProfileId;
        this.pMonitor = pMonitor;
    }

    /**
     * Import the CNA Data.
     *
     * @throws IOException  IO Error.
     * @throws DaoException Database Error.
     */
    public void importData() throws IOException, DaoException {
        DaoGeneticProfile dao = new DaoGeneticProfile();
        DaoMicroRna daoMicroRna = new DaoMicroRna();
        microRnaIdSet = daoMicroRna.getEntireSet();

        geneticProfile = dao.getGeneticProfileById(geneticProfileId);

        FileReader reader = new FileReader(mutationFile);
        BufferedReader buf = new BufferedReader(reader);
        String headerLine = buf.readLine();
        String parts[] = headerLine.split("\t");
        String caseIds[];

        //  Branch, depending on targetLine setting
        if (targetLine == null) {
            caseIds = new String[parts.length - 1];
            System.arraycopy(parts, 1, caseIds, 0, parts.length - 1);
        } else {
            caseIds = new String[parts.length - 2];
            System.arraycopy(parts, 2, caseIds, 0, parts.length - 2);
        }
        pMonitor.setCurrentMessage("Import tab delimited data for " + caseIds.length + " cases.");

        // Add Cases to the Database
        DaoCase daoCase = new DaoCase();
        ArrayList <String> orderedCaseList = new ArrayList<String>();
        for (int i = 0; i < caseIds.length; i++) {
            if (!daoCase.caseExistsInGeneticProfile(caseIds[i],
                    geneticProfileId)) {
                daoCase.addCase(caseIds[i], geneticProfileId);
            }
            orderedCaseList.add(caseIds[i]);
        }
        DaoGeneticProfileCases daoGeneticProfileCases = new DaoGeneticProfileCases();
        daoGeneticProfileCases.addGeneticProfileCases(geneticProfileId, orderedCaseList);

        String line = buf.readLine();
        int numRecordsStored = 0;

        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        DaoMicroRnaAlteration daoMicroRnaAlteration = DaoMicroRnaAlteration.getInstance();

        while (line != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            
            //  Ignore lines starting with #
            if (!line.startsWith("#") && line.trim().length() > 0) {
                parts = line.split("\t");

                int startIndex = getStartIndex();
                String values[] = (String[]) ArrayUtils.subarray(parts, startIndex, parts.length);

                String method = null;
                String geneId = null;
                if (targetLine != null) {
                    method = parts[0];
                    geneId = parts[1];
                } else {
                    geneId = parts[0];
                }

                if (geneId != null) {
                    if (geneId.contains("///") || geneId.contains("---")) {
                        //  Ignore gene IDs separated by ///.  This indicates that
                        //  the line contains information regarding multiple genes, and
                        //  we cannot currently handle this.
                        //  Also, ignore gene IDs that are specified as ---.  This indicates
                        //  the line contains information regarding an unknown gene, and
                        //  we cannot currently handle this.
                    } else {
                        //  Assume we are dealing with Entrez Gene Ids or Symbols.
                        CanonicalGene gene = daoGene.getNonAmbiguousGene(geneId);

                        //  If no target line is specified or we match the target, process.
                        if (targetLine == null || method.equals(targetLine)) {
                            if (gene == null) {

                                //  if gene is null, we might be dealing with a micro RNA ID
                                if (geneId.startsWith("hsa")) {
                                    if (microRnaIdSet.contains(geneId)) {
                                        storeMicroRnaAlterations(values, daoMicroRnaAlteration, geneId);
                                        numRecordsStored++;
                                    } else {
                                        pMonitor.logWarning("microRNA is not known to me:  [" + geneId
                                            + "]. Ignoring it "
                                            + "and all tab-delimited data associated with it!");
                                    }
                                } else {
                                    pMonitor.logWarning("Gene not found:  [" + geneId
                                        + "]. Ignoring it "
                                        + "and all tab-delimited data associated with it!");
                                }
                            } else {
                                storeGeneticAlterations(values, daoGeneticAlteration, gene);
                                numRecordsStored++;
                            }
                        }
                    }
                }
            }
            line = buf.readLine();
        }
        if (MySQLbulkLoader.isBulkLoad()) {
           daoGeneticAlteration.flushGeneticAlteration();
           daoMicroRnaAlteration.flushMicroRnaAlteration();
        }
        
        if (numRecordsStored == 0) {
            throw new DaoException ("Something has gone wrong!  I did not save any records" +
                    " to the database!");
        }
    }

    private void storeMicroRnaAlterations(String[] values,
            DaoMicroRnaAlteration daoMicroRnaAlteration, String microRnaId) throws DaoException {

        //  Check that we have not already imported information regarding this microRNA.
        //  This is an important check, because a GISTIC or RAE file may contain
        //  multiple rows for the same gene, and we only want to import the first row.
        if (!importedMicroRNASet.contains(microRnaId)) {
            daoMicroRnaAlteration.addMicroRnaAlterations(geneticProfileId, microRnaId, values);
            importedMicroRNASet.add(microRnaId);
        }
    }

    private void storeGeneticAlterations(String[] values, DaoGeneticAlteration daoGeneticAlteration,
            CanonicalGene gene) throws DaoException {

        //  Check that we have not already imported information regarding this gene.
        //  This is an important check, because a GISTIC or RAE file may contain
        //  multiple rows for the same gene, and we only want to import the first row.
        if (!importedGeneSet.contains(gene.getEntrezGeneId())) {
            daoGeneticAlteration.addGeneticAlterations(geneticProfileId, gene.getEntrezGeneId(), values);
            importedGeneSet.add(gene.getEntrezGeneId());
        }
    }

    private int getStartIndex() {
        int startIndex = 2;
        if (targetLine == null) {
        startIndex = 1;
    }
        return startIndex;
    }
}
