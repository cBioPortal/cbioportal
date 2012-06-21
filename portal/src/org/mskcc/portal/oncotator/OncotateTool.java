package org.mskcc.portal.oncotator;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.portal.model.MafRecord;
import org.mskcc.portal.util.MafUtil;

import java.io.*;
import java.util.Date;
import java.util.HashMap;

/**
 * Command Line Tool to Oncotate a Single MAF File.
 */
public class OncotateTool {
    private final static String TAB = "\t";
    private int buildNumErrors = 0;
    private OncotatorService oncotatorService = OncotatorService.getInstance();
    private static int MAX_NUM_RECORDS_TO_PROCESS = -1;
    private HashMap<String, Integer> genomicCountMap = new HashMap<String, Integer>();
    private final static long SLEEP_PERIOD = 5000;  // in ms
    
    public OncotateTool(File inputMafFile, File outputMafFile) throws IOException, DaoException {
        outputFileNames(inputMafFile, outputMafFile);
        FileReader reader = new FileReader(inputMafFile);
        BufferedReader bufReader = new BufferedReader(reader);
        String headerLine = bufReader.readLine();
        MafUtil mafUtil = new MafUtil(headerLine);
        String dataLine = bufReader.readLine();
        
        int numRecordsProcessed = 0;
        FileWriter writer = new FileWriter(outputMafFile);
        writeHeaders(headerLine, writer);
        while (dataLine != null) {
            MafRecord mafRecord = mafUtil.parseRecord(dataLine);
            String variantClassification = mafRecord.getVariantClassification();

            writer.write(dataLine.trim());
            //  Skip Silent Mutations
            if (!variantClassification.equalsIgnoreCase("Silent")) {
                conditionallyOncotateRecord(mafRecord, writer);
                numRecordsProcessed++;
                conditionallyAbort(numRecordsProcessed);
            } else {
                writeEmptyDataFields(writer);
            }
            writer.write("\n");
            try {
                //  Must go to sleep;  otherwise, we trigger the Broad's Limit.
                Thread.sleep(SLEEP_PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            dataLine = bufReader.readLine();
        }
        System.out.println("Total Number of Records Processed:  " + numRecordsProcessed);
        for (String coords:  genomicCountMap.keySet()) {
            Integer count = genomicCountMap.get(coords);
            if (count > 1) {
                System.out.println(coords + "\t" + (count-1));
            }
        }
    }

    private void outputFileNames(File inputMafFile, File outputMafFile) {
        System.out.println("Reading MAF From:  " + inputMafFile.getAbsolutePath());
        System.out.println("Writing new MAF To:  " + outputMafFile.getAbsolutePath());
    }

    private void conditionallyAbort(int numRecordsProcessed) {
        if (MAX_NUM_RECORDS_TO_PROCESS > 0 && numRecordsProcessed > MAX_NUM_RECORDS_TO_PROCESS) {
            throw new IllegalStateException("Aborting at " + MAX_NUM_RECORDS_TO_PROCESS + " records");
        }
    }

    private void writeEmptyDataFields(FileWriter writer) throws IOException {
        for (int i=0; i<4; i++) {
            writer.write(TAB + "");
        }
    }

    private void writeHeaders(String headerLine, FileWriter writer) throws IOException {
        writer.write(headerLine.trim());
        writer.write(TAB + "ONCOTATOR_VARIANT_CLASSIFICATION");
        writer.write(TAB + "ONCOTATOR_PROTEIN_CHANGE");
        writer.write(TAB + "ONCOTATOR_COSMIC_OVERLAPPING");
        writer.write(TAB + "ONCOTATOR_DBSNP_RS");
        writer.write("\n");
    }

    private void conditionallyOncotateRecord(MafRecord mafRecord, FileWriter writer) throws IOException, DaoException {
        String ncbiBuild = mafRecord.getNcbiBuild();
        if (!ncbiBuild.equals("37")) {
            outputBuildNumErrorMessage(ncbiBuild);
            buildNumErrors++;
            if (buildNumErrors > 10) {
                abortDueToBuildNumErrors();
            }
        } else {
            oncotateRecord(mafRecord, writer);
        }
    }

    private void abortDueToBuildNumErrors() {
        System.out.println("Too many records with wrong build #.  Aborting...");
        System.exit(1);
    }

    private void outputBuildNumErrorMessage(String ncbiBuild) {
        System.out.println("Record uses NCBI Build:  " + ncbiBuild);
        System.out.println("-->  Oncotator only works with Build 37/hg19.");
    }

    private void oncotateRecord(MafRecord mafRecord, FileWriter writer) throws IOException, DaoException {
        String chr = mafRecord.getChr();
        long start = mafRecord.getStartPosition();
        long end = mafRecord.getEndPosition();
        String refAllele = mafRecord.getReferenceAllele();
        String tumorAllele = determineTumorAllele(mafRecord, refAllele);
        if (tumorAllele != null) {
            String coords = createCoordinates(chr, start, end, refAllele, tumorAllele);
            System.out.println(coords);
            if (genomicCountMap.containsKey(coords)) {
                Integer count = genomicCountMap.get(coords);
                genomicCountMap.put(coords, count+1);
            } else {
                genomicCountMap.put(coords, 1);
            }
            OncotatorRecord oncotatorRecord =
                    oncotatorService.getOncotatorRecord(chr, start, end, refAllele,tumorAllele);
            writeOncotatorResults(writer, oncotatorRecord);
        } else {
            writeEmptyDataFields(writer);
        }
    }

    private String determineTumorAllele(MafRecord mafRecord, String refAllele) {
        String tumorAllel1 = mafRecord.getTumorSeqAllele1();
        String tumorAllel2 = mafRecord.getTumorSeqAllele2();
        String tumorAllele = null;
        if (!refAllele.equals(tumorAllel1)) {
            tumorAllele = tumorAllel1;
        } else if(!refAllele.equalsIgnoreCase(tumorAllel2)) {
            tumorAllele = tumorAllel2;
        }
        return tumorAllele;
    }

    private void writeOncotatorResults(Writer writer, OncotatorRecord oncotatorRecord) throws IOException {
        String proteinChange = oncotatorRecord.getProteinChange();
        String cosmicOverlapping = oncotatorRecord.getCosmicOverlappingMutations();
        String dbSnpRs = oncotatorRecord.getDbSnpRs();
        String variantClassification = oncotatorRecord.getVariantClassification();
        writer.write(TAB + outputField(variantClassification));
        writer.write(TAB + outputField(proteinChange));
        writer.write(TAB + outputField(cosmicOverlapping));
        writer.write(TAB + outputField(dbSnpRs));
    }
    
    private String outputField(String field) {
        if (field == null) {
            return "";
        } else {
            return field;
        }
    }

    private String createCoordinates(String chr, long start, long end,
                                     String refAllele, String tumorAllele) {
        return chr + "_" + start + "_" + end + "_" + refAllele 
                + "_" + tumorAllele;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("command line usage:  oncotateMaf.pl <file.maf> <file_output.maf>");
            System.exit(1);
        }
        Date start = new Date();
        try {
            OncotateTool tool = new OncotateTool(new File(args[0]), new File(args[1]));
        } catch (Exception e) {
            System.out.println("Error occurred:  " + e.getMessage());
        } finally {
            Date end = new Date();
            double timeElapsed = (end.getTime() - start.getTime()) / 1000.0;
            System.out.println("Total time:  " + timeElapsed + " seconds.");
        }
    }
}