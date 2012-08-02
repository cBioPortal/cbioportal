package org.mskcc.cbio.liftover;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;

/**
 * Pre-process class for the lift over tool. Extracts information from and input MAF file,
 * and creates an output file compatible with the lift over tool.
 * Each line of the output file represents a mutation:
 *  chr# chr_start chr_end
 * 
 * @author Selcuk Onur Sumer
 *
 */
public class PreLiftOver
{
	/**
	 * Extracts information from the given MAF to create an input
	 * compatible with liftOver tool.
	 *
	 * @param inputMaf  source MAF file
	 */
	public static void prepareInput(String inputMaf,
			String inFile,
			String auxFile)
	{
		try
		{
			extractPositions(inputMaf,
				inFile,
				auxFile);
		}
		catch (IOException e)
		{
			System.out.println("Error while processing input: " + 
					e.getMessage());
			
			e.printStackTrace();
		}
	}
	
	/**
	 * Extracts start & end position information from the given input (MAF)
	 * file and creates the main output file. Also, creates an aux file
	 * to store the row number with adjusted positions (for lift over)  
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @param auxFile
	 * @throws java.io.IOException
	 */
	public static void extractPositions(String inputFile,
			String outputFile,
			String auxFile) throws IOException
	{
		BufferedReader bufReader = new BufferedReader(
        		new FileReader(inputFile));
		BufferedWriter bufWriter = new BufferedWriter(
				new FileWriter(outputFile));
		BufferedWriter auxWriter = new BufferedWriter(
				new FileWriter(auxFile));				
		
        String headerLine = bufReader.readLine();
        MafUtil util = new MafUtil(headerLine);
        String line;
        MafRecord record;
        
        long startPos, endPos;
        String chr;
        int row = 2; // including header
        
        while ((line = bufReader.readLine()) != null)
        {
        	record = util.parseRecord(line);
        	startPos = record.getStartPosition();
        	endPos = record.getEndPosition();
        	
        	// if start&end pos are the same, then the coordinate range
        	// in BED format specifies a region of size 0.
        	// BED coordinates are zero-based, half-open.  See:
        	// http://genome.ucsc.edu/FAQ/FAQformat.html#format1
        	// and
        	// http://genomewiki.ucsc.edu/index.php/Coordinate_Transforms 
        	if (startPos == endPos)
        	{
        		// adjust start position
        		startPos--;
        		
        		// write line number to the aux file for reference in PostLiftOver
        		// to the adjusted lines
        		auxWriter.write(Integer.toString(row));
        		auxWriter.newLine();
        	}
        	
        	chr = record.getChr();
        	
        	// chrMT is not recognized so replace it with M
        	if (chr.equals("MT"))
        	{
        		chr = "M";
        	}
        	
        	bufWriter.write("chr" + chr + "\t" + startPos + "\t" + endPos);
        	bufWriter.newLine();
        	
        	row++;
        }
        
        bufReader.close();
        bufWriter.close();
        auxWriter.close();
	}
}
