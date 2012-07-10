package org.mskcc.cgds.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.mskcc.portal.model.MafRecord;
import org.mskcc.portal.util.MafUtil;

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
	public static void main(String[] args)
	{
		String inputMaf = args[0];
		
		try
		{
			extractPositions(inputMaf, "oldfile.txt");
		}
		catch (IOException e)
		{
			System.out.println("Error while processing input: " + 
					e.getMessage());
			
			e.printStackTrace();
		}
	}
	
	public static void extractPositions(String inputFile,
			String outputFile) throws IOException
	{
		BufferedReader bufReader = new BufferedReader(
        		new FileReader(inputFile));
		BufferedWriter bufWriter = new BufferedWriter(
				new FileWriter(outputFile));
		
        String headerLine = bufReader.readLine();
        MafUtil util = new MafUtil(headerLine);
        String line;
        MafRecord record;
        
        long startPos, endPos;
        String chr;
        
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
        		startPos--;
        	}
        	
        	chr = record.getChr();
        	
        	// chrMT is not recognized so replace it with M
        	if (chr.equals("MT"))
        	{
        		chr = "M";
        	}
        	
        	bufWriter.write("chr" + chr + "\t" + startPos + "\t" + endPos);
        	bufWriter.newLine();
        }
        
        bufReader.close();
        bufWriter.close();
	}
}
