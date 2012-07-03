package org.mskcc.cgds.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.mskcc.portal.model.MafRecord;
import org.mskcc.portal.util.MafUtil;

/**
 * Post-process class for the lift over tool. Updates coordinates of each
 * mutation in the input MAF file by using the output files created by the
 * left over tool.
 * 
 * @author Selcuk Onur Sumer
 *
 */
public class PostLiftOver
{
	
	public static void main(String[] args)
	{
		System.out.println("original file: " + args[0]);
		System.out.println("mapped file: " + args[1]);
		System.out.println("unmapped file: " + args[2]);
		System.out.println("output maf: " + args[3]);
	}
	
	public static void updatePositions(String inputFile,
			String mappedFile,
			String unmappedFile,
			String outputFile) throws IOException
	{
		BufferedReader sourceIn = new BufferedReader(
        		new FileReader(inputFile));
		BufferedReader mappedIn = new BufferedReader(
        		new FileReader(mappedFile));
		BufferedReader unmappedIn = new BufferedReader(
        		new FileReader(mappedFile));
		
		BufferedWriter bufWriter = new BufferedWriter(
				new FileWriter(outputFile));
		
        String headerLine = sourceIn.readLine();
        MafUtil util = new MafUtil(headerLine);
        String sourceLine;
        String mappedLine = mappedIn.readLine();
        String unmappedLine = getUnmappedLine(unmappedIn);
        MafRecord record;
        
        while ((sourceLine = sourceIn.readLine()) != null)
        {
        	// get a single record per line
        	record = util.parseRecord(sourceLine);
        	
        	// check if current record matches the unmapped file
        	if (unmappedLine != null &&
        		isUnmapped(record, unmappedLine))
        	{
        		// TODO what to do with unmapped records?
        		
        		// get next line from unmapped file
        		unmappedLine = getUnmappedLine(unmappedIn);
        	}
        	else
        	{
        		String[] mappedParts = mappedLine.split("//s");
        		
        		if (mappedParts[0].equals("chr" + record.getChr()))
        		{
        			// TODO update record and write to output
        		}
        		else
        		{
        			// TODO there is something wrong...
        		}
        		
        		// get next line from mapped file
        		mappedLine = mappedIn.readLine();
        	}
        }
        
        sourceIn.close();
        mappedIn.close();
        unmappedIn.close();
        bufWriter.close();
	}
	
	private static boolean isUnmapped(MafRecord record,
			String unmappedLine)
	{
		boolean unmapped = false;
		String[] parts = unmappedLine.split("//s");
		
		// compare chr, start & end positions
		if (parts[0].equals("chr" + record.getChr()) &&
			record.getStartPosition() == Long.parseLong(parts[1]) &&
			record.getEndPosition() == Long.parseLong(parts[2]))
		{
			unmapped = true;
		}
		
		return unmapped;
	}
	
	private static String getUnmappedLine(BufferedReader unmappedIn) throws IOException
	{
		String unmappedLine = unmappedIn.readLine();
		
		while (unmappedLine != null &&
				unmappedLine.trim().startsWith("#"))
		{
			unmappedLine = unmappedIn.readLine();
		}
		
		return unmappedLine;
	}
}
