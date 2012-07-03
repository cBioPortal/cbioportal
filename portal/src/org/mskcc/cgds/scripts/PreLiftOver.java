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
        
        while ((line = bufReader.readLine()) != null)
        {
        	record = util.parseRecord(line);
        	bufWriter.write("chr" + record.getChr() + " " + record.getStartPosition() + " " + record.getEndPosition());
        	bufWriter.newLine();
        }
        
        bufReader.close();
        bufWriter.close();
	}
}
