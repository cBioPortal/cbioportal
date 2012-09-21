/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.oncotator;


import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;

import java.io.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

/**
 * Command Line Tool to Oncotate a Single MAF File.
 */
public class OncotateTool {
    private final static String TAB = "\t";
    private int buildNumErrors = 0;
    private OncotatorService oncotatorService;
    private static int MAX_NUM_RECORDS_TO_PROCESS = -1;
    private static int ONCO_HEADERS_COUNT = 5;
    private HashMap<String, Integer> genomicCountMap;

    public OncotateTool()
    {
	    this.oncotatorService = OncotatorService.getInstance();
	    this.genomicCountMap = new HashMap<String, Integer>();
    }

	private int oncotateMaf(File inputMafFile,
			File outputMafFile) throws IOException, SQLException
	{
		outputFileNames(inputMafFile, outputMafFile);

		FileReader reader = new FileReader(inputMafFile);
		BufferedReader bufReader = new BufferedReader(reader);
		String headerLine = bufReader.readLine();
		MafUtil mafUtil = new MafUtil(headerLine);
		String dataLine = bufReader.readLine();

		int numRecordsProcessed = 0;
		FileWriter writer = new FileWriter(outputMafFile);

		writeHeaders(headerLine, writer);

		while (dataLine != null)
		{
			MafRecord mafRecord = mafUtil.parseRecord(dataLine);
			String variantClassification = mafRecord.getVariantClassification();

			// adjust data line before writing to make sure the consistency
			// among the lines
			writer.write(this.adjustDataLine(dataLine, mafUtil));

			//  Skip Silent Mutations
			if (!variantClassification.equalsIgnoreCase("Silent")) {
				conditionallyOncotateRecord(mafRecord, writer);
				numRecordsProcessed++;
				conditionallyAbort(numRecordsProcessed);
			} else {
				writeEmptyDataFields(writer);
			}
			writer.write("\n");
			dataLine = bufReader.readLine();
		}

		System.out.println("Total Number of Records Processed:  " + numRecordsProcessed);
		for (String coords:  genomicCountMap.keySet()) {
			Integer count = genomicCountMap.get(coords);
			if (count > 1) {
				System.out.println(coords + "\t" + (count-1));
			}
		}

		reader.close();
		writer.close();

		return this.oncotatorService.getErrorCount();
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
        for (int i=0; i<ONCO_HEADERS_COUNT; i++) {
            writer.write(TAB + "");
        }
    }

    private void writeHeaders(String headerLine, FileWriter writer) throws IOException
    {
        writer.write(headerLine.trim());
        
        // write new headers only if the oncotator headers do not already exist
        // in the current MAF file (assuming if it contains one of the oncotator
        // headers, then it contains all of them)
        if (!headerLine.contains("ONCOTATOR_VARIANT_CLASSIFICATION"))
        {
        	writer.write(TAB + "ONCOTATOR_VARIANT_CLASSIFICATION");
            writer.write(TAB + "ONCOTATOR_PROTEIN_CHANGE");
            writer.write(TAB + "ONCOTATOR_COSMIC_OVERLAPPING");
            writer.write(TAB + "ONCOTATOR_DBSNP_RS");
            writer.write(TAB + "ONCOTATOR_GENE_SYMBOL");
        }
        
        writer.write("\n");
    }

    private void conditionallyOncotateRecord(MafRecord mafRecord,
		    FileWriter writer) throws IOException, SQLException
    {
        String ncbiBuild = mafRecord.getNcbiBuild();

	    if (!ncbiBuild.equals("37") &&
	        !ncbiBuild.equalsIgnoreCase("hg19"))
	    {
            outputBuildNumErrorMessage(ncbiBuild);
            buildNumErrors++;

            if (buildNumErrors > 10) {
                abortDueToBuildNumErrors();
            }
        }
	    else
	    {
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

    private void oncotateRecord(MafRecord mafRecord,
		    FileWriter writer) throws IOException, SQLException
    {
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

    private String determineTumorAllele(MafRecord mafRecord,
		    String refAllele)
    {
        String tumorAllel1 = mafRecord.getTumorSeqAllele1();
        String tumorAllel2 = mafRecord.getTumorSeqAllele2();
        String tumorAllele = null;
        if (!refAllele.equalsIgnoreCase(tumorAllel1)) {
            tumorAllele = tumorAllel1;
        } else if(!refAllele.equalsIgnoreCase(tumorAllel2)) {
            tumorAllele = tumorAllel2;
        }
        return tumorAllele;
    }

    private void writeOncotatorResults(Writer writer,
		    OncotatorRecord oncotatorRecord) throws IOException
    {
        String proteinChange = oncotatorRecord.getProteinChange();
        String cosmicOverlapping = oncotatorRecord.getCosmicOverlappingMutations();
        String dbSnpRs = oncotatorRecord.getDbSnpRs();
        String variantClassification = oncotatorRecord.getVariantClassification();
        String geneSymbol = oncotatorRecord.getGene();
        writer.write(TAB + outputField(variantClassification));
        writer.write(TAB + outputField(proteinChange));
        writer.write(TAB + outputField(cosmicOverlapping));
        writer.write(TAB + outputField(dbSnpRs));
        writer.write(TAB + outputField(geneSymbol));
    }
    
    private String outputField(String field)
    {
        if (field == null) {
            return "";
        } else {
            return field;
        }
    }

    private String createCoordinates(String chr, long start, long end,
                                     String refAllele, String tumorAllele)
    {
        return chr + "_" + start + "_" + end + "_" + refAllele 
                + "_" + tumorAllele;
    }
    
    /**
     * Adjusts the data line for consistency.
     * 
     * If the data is already oncotated removes last ONCO_HEADERS_COUNT columns
     * to enable re-oncotation. Otherwise adjusts the data line to have columns
     * exactly the same as the number of column headers to prevent incorrect
     * oncotating.
     * 
     * @param dataLine	line to be adjusted
     * @param util		MAF util containing header information
     * @return			adjusted data line
     */
    private String adjustDataLine(String dataLine, MafUtil util)
    {
    	//String line = dataLine.trim();
    	String line = new String(dataLine);
    	String[] parts = line.split(TAB, -1);
    	
    	// diff should be zero if (# of headers == # of data cols)
    	int diff = util.getHeaderCount() - parts.length;
    	
    	// check if already oncotated
    	boolean oncotated = (util.getOncoVariantClassificationIndex() != -1);
    	
    	// file already oncotated
    	if (oncotated)
    	{
        	line = new String();
        	
    		// remove last ONCO_HEADERS_COUNT data columns
    		// (to enable overwrite instead of appending new cols to the end)
    		for (int i = 0; i < parts.length - ONCO_HEADERS_COUNT; i++)
    		{
    			line += parts[i];
    			
    			if (i != parts.length - ONCO_HEADERS_COUNT - 1)
    			{
    				line += TAB;
    			}
    				
    		}
    	}
    	// not oncotated, but header and data mismatch
    	else if (diff > 0)
    	{
    		// append appropriate number of tabs
    		for (int i = 0; i < diff; i++)
    		{
    			line += TAB;
    		}
    	}
    	
    	return line;
    }

    public static void main(String[] args)
    {
        if (args.length < 2)
        {
            System.out.println("command line usage: oncotateMaf.sh <input_maf_file> <output_maf_file");
            System.exit(1);
        }

        Date start = new Date();
	    int oncoResult = 0;

        try {
            OncotateTool tool = new OncotateTool();
	        oncoResult = tool.oncotateMaf(new File(args[0]), new File(args[1]));
        }
        catch (Exception e)
        {
            System.out.println("Error occurred:  " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            Date end = new Date();
            double timeElapsed = (end.getTime() - start.getTime()) / 1000.0;
            System.out.println("Total time:  " + timeElapsed + " seconds.");

	        // check errors at the end
	        if (oncoResult != 0)
	        {
		        // TODO produce different error codes, for different types of errors?
		        System.out.println("Process completed with " + oncoResult + " error(s).");
		        System.exit(2);
	        }
        }
    }
}