package org.mskcc.cgds.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.mskcc.portal.model.MafRecord;
import org.mskcc.portal.util.MafUtil;

public class PreLiftOver
{
	public static void main(String[] args)
	{
		String inputMaf = args[0];
		
		try {
			extractPositions(inputMaf, "oldfile.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
