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

package org.mskcc.cbio.liftover;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;

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
	/**
	 * Updates the original MAF file by using the information generated
	 * by the liftOver tool.
	 *
	 * @param originalFile  original input (MAF) file
	 * @param mappedFile    mapped file created by the liftOver tool
	 * @param unmappedFile  unmapped file created by the liftOver tool
	 * @param auxFile       aux file to store modified rows
	 * @param outputMaf     name of the new output file to be created
	 */
	public static int updateMaf(String originalFile,
		String mappedFile,
		String unmappedFile,
		String auxFile,
		String outputMaf)
	{
		try
		{
			// sanity check: assert (# of entries in original file) ==
			// (total # of entries in both mapped & unmapped file)
			// if not, then it means there is an error during lift over process
			if (sizeMismatch(originalFile,
					mappedFile,
					unmappedFile))
			{
				System.out.println("Input sizes do not match! Error while lifting over?");
				return 1;
			}
			else
			{
				updatePositions(originalFile,
						mappedFile,
						unmappedFile,
						auxFile,
						outputMaf);
			}
			
		}
		catch (IOException e)
		{
			System.out.println("Error while processing files: " + e.getMessage());
			e.printStackTrace();
			return 2;
		}

		return 0;
	}
	
	/**
	 * Updates the positions of the source input file by using mapped and
	 * unmapped outputs. Writes the results to the given output file. This
	 * function assumes that liftOver script runs without any problems.
	 * In other words, this function works correctly if the number of entries
	 * in the input file is equal to the total number of entries in the
	 * mapped and unmapped files. Otherwise resulting output MAF file may
	 * contain incorrect information.
	 * 
	 * @param inputFile		original input MAF file
	 * @param mappedFile	mapped file created by liftOver
	 * @param unmappedFile	unmapped file created by liftOver
	 * @param outputFile	output file with updated coordinates
	 * @param auxFile		aux file containing info of position-adjusted rows
	 * @throws java.io.IOException	if an IO error occurs
	 */
	public static void updatePositions(String inputFile,
			String mappedFile,
			String unmappedFile,
			String auxFile,
			String outputFile) throws IOException
	{
		BufferedReader sourceIn = new BufferedReader(
        		new FileReader(inputFile));
		BufferedReader mappedIn = new BufferedReader(
        		new FileReader(mappedFile));
		BufferedReader unmappedIn = new BufferedReader(
        		new FileReader(unmappedFile));
		BufferedReader auxIn = new BufferedReader(
        		new FileReader(auxFile));
		
		BufferedWriter bufWriter = new BufferedWriter(
				new FileWriter(outputFile));
		
        String headerLine = sourceIn.readLine();
        MafUtil util = new MafUtil(headerLine);
        String sourceLine;
        String mappedLine = mappedIn.readLine();
        String unmappedLine = getUnmappedLine(unmappedIn);
        Integer modifiedRow = getNextModRow(auxIn);
        MafRecord record;
        
        // for tracking purposes
        int sourceRow = 2; // (including header)
        int mappedRow = 1;
        int unmappedRow = 1;
        boolean modified;
        
        // write the header line to the output
        bufWriter.write(headerLine);
        bufWriter.newLine();
        
        while ((sourceLine = sourceIn.readLine()) != null)
        {
        	// get a single record per line
        	record = util.parseRecord(sourceLine);
        	
        	// check if positions of current record is modified by PreLiftOver
        	if (modifiedRow != null &&
        		sourceRow == modifiedRow)
        	{
        		modified = true;
        		modifiedRow = getNextModRow(auxIn);
        	}
        	else
        	{
        		modified = false;
        	}
        	
        	// check if current record matches the unmapped file
        	if (unmappedLine != null &&
        		isUnmapped(record, unmappedLine))
        	{
        		System.out.println("[warning] cannot lift over (line:" + sourceRow + "): " +
        				"[gene:" + record.getHugoGeneSymbol() + "] " + unmappedLine);
        		
        		// skip unmapped lines (do not include in the output file)
        		// bufWriter.write(sourceLine);
        		
        		// get next line from unmapped file
        		unmappedLine = getUnmappedLine(unmappedIn);
        		
        		unmappedRow++;
        	}
        	// record is lifted over successfully, update the positions 
        	else
        	{
        		String[] mappedParts = mappedLine.split("\\s");
        		
        		String sourceChr = record.getChr();
        		
        		if (sourceChr.equals("MT"))
        		{
        			sourceChr = "M";
        		}
        		
    			StringBuffer buffer = new StringBuffer();
    			
    			long startPos = Long.parseLong(mappedParts[1]);
    			long endPos = Long.parseLong(mappedParts[2]);
    			boolean chrChanged = false;
    			
        		if (!mappedParts[0].equals("chr" + sourceChr))
        		{
            		// print a warning message about the chromosome number change..
            		System.out.println("[warning] chromosome numbers mismatch (gene:" + record.getHugoGeneSymbol() + ")");
            		System.out.println("source(line:" + sourceRow + "): chr" + sourceChr + " " + 
            				record.getStartPosition() + " " +
            				record.getEndPosition());
            		System.out.println("mapped(line:" + mappedRow + "): " + mappedLine);
            		
            		chrChanged = true;
        		}
    			
        		// re-adjust start & end positions (to be compatible with oncotator)
        		if (modified)
        		{
        			startPos++;
        		}
        		
        		// below condition check may also work for all records,
        		// but using modified row number is the safest way.   		
//    			if (startPos == endPos - 1 &&
//    				!record.getReferenceAllele().equals("-") &&
//    				record.getReferenceAllele().length() == 1)
//    			{
//    				startPos++;
//    			}
    			
    			String sourceParts[] = sourceLine.split("\t", -1);
    			
    			// update start & end positions
    			for (int i = 0; i < sourceParts.length; i++)
    			{
					if (util.getStartPositionIndex() == i)
					{
						// replace with new start position
						buffer.append(startPos);
					}
					else if (util.getEndPositionIndex() == i)
					{
						// replace with new end position
						buffer.append(endPos);
					}
					else if (chrChanged &&
							util.getChrIndex() == i)
					{
						// replace with new chr number
						buffer.append(mappedParts[0].replaceAll("chr", ""));
					}
					else if (util.getNcbiIndex() == i)
					{
						// also update the build from 18 to 19 (36 to 37)
						buffer.append(record.getNcbiBuild().replace(
								"18", "19").replace("36.1", "37").replace(
										"36", "37"));
					}
					else
					{
						// just copy the original content
						buffer.append(sourceParts[i]);
					}
					
					if (i < sourceParts.length - 1)
					{
						buffer.append("\t");
					}
				}
    			
    			// update record and write to output
    			bufWriter.write(buffer.toString());
    			bufWriter.newLine();
        		
        		// get next line from mapped file
        		mappedLine = mappedIn.readLine();
        		
        		mappedRow++;
        	}
        	
        	sourceRow++;
        }
        
        sourceIn.close();
        mappedIn.close();
        unmappedIn.close();
        auxIn.close();
        bufWriter.close();
	}
	
	/**
	 * Compares original input file to the two output files created by
	 * the liftOver tool to verify that number of entries in the original
	 * input file is equal to total number of entries in both the mapped
	 * and the unmapped files. 
	 * 
	 * @param inputFile		original input
	 * @param mappedFile	mapped file (created by liftOver)
	 * @param unmappedFile	unmapped file (created by liftOver)
	 * @return				true if sizes mismatch, false otherwise
	 * @throws java.io.IOException	if an IO error occurs
	 */
	private static boolean sizeMismatch(String inputFile,
			String mappedFile,
			String unmappedFile) throws IOException
	{
		BufferedReader sourceIn = new BufferedReader(
        		new FileReader(inputFile));
		BufferedReader mappedIn = new BufferedReader(
        		new FileReader(mappedFile));
		BufferedReader unmappedIn = new BufferedReader(
        		new FileReader(unmappedFile));
		
		int sourceCount = 0;
		int mappedCount = 0;
		int unmappedCount = 0;
		
		while (sourceIn.readLine() != null)
		{
			sourceCount++;
		}
		
		sourceIn.close();
		
		while (mappedIn.readLine() != null)
		{
			mappedCount++;
		}
		
		mappedIn.close();
		
		while (getUnmappedLine(unmappedIn) != null)
		{
			unmappedCount++;
		}
		
		unmappedIn.close();
		
		
		boolean mismatch = true;
		
		if (sourceCount - 1 == mappedCount + unmappedCount)
		{
			mismatch = false;
		}
		
		return mismatch;
	}
	
	/**
	 * Checks if the given record is in the unmapped file (created by liftOver).
	 * 
	 * @param record		record to be checked
	 * @param unmappedLine	a line from the unmapped file
	 * @return				true if the record is unmapped, false otherwise
	 */
	private static boolean isUnmapped(MafRecord record,
			String unmappedLine)
	{
		boolean unmapped = false;
		String[] parts = unmappedLine.split("\\s");
    	
    	// adjust startPos & chr to match liftOver output
    	// (because they are adjusted in PreLiftOver to create an input
		// compatible with liftOver tool) 
    	
		long startPos = record.getStartPosition();
    	long endPos = record.getEndPosition();
    	
    	if (startPos == endPos)
    	{
    		startPos--;
    	}
    	
    	String chr = record.getChr();
    	
    	if (chr.equals("MT"))
    	{
    		chr = "M";
    	}
		
		// compare chr, start & end positions
		if (parts[0].equals("chr" + chr) &&
			startPos == Long.parseLong(parts[1]) &&
			endPos == Long.parseLong(parts[2]))
		{
			unmapped = true;
		}
		
		return unmapped;
	}
	
	/**
	 * Skips any comment lines and retrieves a data line from the unmapped file
	 * (created by liftOver).
	 * 
	 * @param unmappedIn	input reader for the unmapped file
	 * @return				a data line from the unmapped file, or null if EOF
	 * @throws java.io.IOException	if an IO error occurs
	 */
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
	
	/**
	 * Retrieves the next modified row (record) number from the aux file.
	 * If there is no more row number, then returns null.
	 * 
	 * @param auxIn			aux file reader
	 * @return				next modified row number
	 * @throws java.io.IOException	if an IO error occurs
	 */
	private static Integer getNextModRow(BufferedReader auxIn) throws IOException
	{
		String line = auxIn.readLine();
		Integer row = null;
		
		if (line != null)
		{
			row = Integer.parseInt(line);
		}
		
		return row;
	}
}
