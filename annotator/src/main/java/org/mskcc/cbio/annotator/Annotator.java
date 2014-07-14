package org.mskcc.cbio.annotator;

import org.mskcc.cbio.maf.*;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Main class for adding generic annotations.
 * This class depends on some external (non-Java) scripts.
 *
 * @author Selcuk Onur Sumer
 */
public class Annotator
{
	// config params (TODO create a config class instead?)
	protected boolean sortColumns;
	protected boolean addMissingCols;
	protected String vepPath;
	protected String maf2MafScript;
	protected String vcf2MafScript;

	// intermediate annotator files
	public static final String DEFAULT_INTERMEDIATE_MAF = "annotator_out.maf";
	public static final String DEFAULT_INTERMEDIATE_DIR = "annotator_dir";
	public static final String DEFAULT_MAF2MAF = "maf2maf.pl";
	public static final String DEFAULT_VCF2MAF = "vcf2maf.pl";
	public static final String DEFAULT_VEP_PATH = "";

	public Annotator()
	{
		// init default settings
		this.sortColumns = false;
		this.addMissingCols = false;
		this.maf2MafScript = DEFAULT_MAF2MAF;
		this.vcf2MafScript = DEFAULT_VCF2MAF;
	}

	public void annotateFile(File input,
			File output) throws IOException
	{
		int retVal = -1;

		// script to run depends on the extension
		if (input.getName().toLowerCase().endsWith(".vcf"))
		{
			retVal = this.runVcf2Maf(input);
			return;
		}
		// assuming it is a maf..
		else
		{
			retVal = this.runMaf2Maf(input);
		}

		// TODO check return value?

		List<String> annoHeaders = this.extractAnnoHeaders(DEFAULT_INTERMEDIATE_MAF);

		FileReader reader = new FileReader(input);
		//FileReader reader = new FileReader(DEFAULT_INTERMEDIATE_MAF);

		BufferedReader bufReader = new BufferedReader(reader);
		MafHeaderUtil headerUtil = new MafHeaderUtil();

		String headerLine = headerUtil.extractHeader(bufReader);
		MafUtil mafUtil = new MafUtil(headerLine);

		AnnoMafProcessor processor = new AnnoMafProcessor(headerLine, annoHeaders);

		FileWriter writer = new FileWriter(output);

		// write comments/metadata to the output
		FileIOUtil.writeLines(writer, headerUtil.getComments());

		// create new header line for output
		List<String> columnNames = processor.newHeaderList(
				this.sortColumns, this.addMissingCols);

		// write the header line to output
		FileIOUtil.writeLine(writer, columnNames);

		String dataLine = bufReader.readLine();
		AnnotatorService service = new AnnotatorService();

		// process the file line by line
		while (dataLine != null)
		{
			// skip empty lines
			if (dataLine.trim().length() == 0)
			{
				dataLine = bufReader.readLine();
				continue;
			}

			// update total number of records processed
			//this.numRecordsProcessed++;

			MafRecord mafRecord = mafUtil.parseRecord(dataLine);
			Map<String, String> annoData = service.annotateRecord(mafRecord);

			// get the data and update/add new oncotator columns
			List<String> data = processor.newDataList(dataLine);

			processor.updateAnnoData(data, annoData);

			// write data to the output file
			FileIOUtil.writeLine(writer, data);

			dataLine = bufReader.readLine();
		}

		reader.close();
		writer.close();

	}

	public int runMaf2Maf(File input) throws IOException
	{
		String inputMaf = input.getAbsolutePath();

		// TODO enable configuration of hard-coded params
		String interDir = DEFAULT_INTERMEDIATE_DIR;
		String outMaf = DEFAULT_INTERMEDIATE_MAF;

		String[] args = {
			"perl",
			this.getMaf2MafScript(),
			"--vep-path",
			this.getVepPath(),
			"--input-maf",
			inputMaf,
			"--output-dir",
			interDir,
			"--output-maf",
			outMaf
		};

		return execProcess(args);
	}

	public int runVcf2Maf(File input) throws IOException
	{
		String inVcf = input.getAbsolutePath();

		// TODO enable configuration of hard-coded params
		String outMaf = DEFAULT_INTERMEDIATE_MAF;

		String[] args = {
			"perl",
			this.getVcf2MafScript(),
			"--vep-path",
			this.getVepPath(),
			"--input-vcf",
			inVcf,
			"--output-maf",
			outMaf
		};

		return execProcess(args);
	}

	// TODO code duplication! -- we have the same code in liftover module
	/**
	 * Executes an external process via system call.
	 *
	 * @param args          process arguments (including the process itself)
	 * @return              exit value of the process
	 * @throws IOException  if an IO error occurs
	 */
	public static int execProcess(String[] args) throws IOException
	{
		Process process = Runtime.getRuntime().exec(args);

		InputStream stdin = process.getInputStream();
		InputStream stderr = process.getErrorStream();
		InputStreamReader isr = new InputStreamReader(stdin);
		InputStreamReader esr = new InputStreamReader(stderr);
		BufferedReader inReader = new BufferedReader(isr);
		BufferedReader errReader = new BufferedReader(esr);

		// echo output messages to stdout
		String line = null;

		while ((line = inReader.readLine()) != null)
		{
			System.out.println(line);
		}

		// also echo error messages
		while ((line = errReader.readLine()) != null)
		{
			System.out.println(line);
		}

		int exitValue = -1;

		// wait for process to complete
		try
		{
			exitValue = process.waitFor();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		return exitValue;
	}

	protected void outputFileNames(File input, File output)
	{
		System.out.println("Reading input from: " + input.getAbsolutePath());
		System.out.println("Writing output to: " + output.getAbsolutePath());
	}

	protected List<String> extractAnnoHeaders(String input) throws IOException
	{
		FileReader reader = new FileReader(input);

		BufferedReader bufReader = new BufferedReader(reader);
		MafHeaderUtil headerUtil = new MafHeaderUtil();

		String headerLine = headerUtil.extractHeader(bufReader);
		String parts[] = headerLine.split("\t");

		reader.close();

		return Arrays.asList(parts);
	}

	// Getters and Setters

	public boolean isSortColumns()
	{
		return sortColumns;
	}

	public void setSortColumns(boolean sortColumns)
	{
		this.sortColumns = sortColumns;
	}

	public boolean isAddMissingCols()
	{
		return addMissingCols;
	}

	public void setAddMissingCols(boolean addMissingCols)
	{
		this.addMissingCols = addMissingCols;
	}

	public String getVepPath()
	{
		return vepPath;
	}

	public void setVepPath(String vepPath)
	{
		this.vepPath = vepPath;
	}

	public String getMaf2MafScript()
	{
		return maf2MafScript;
	}

	public void setMaf2MafScript(String maf2MafScript)
	{
		this.maf2MafScript = maf2MafScript;
	}

	public String getVcf2MafScript()
	{
		return vcf2MafScript;
	}

	public void setVcf2MafScript(String vcf2MafScript)
	{
		this.vcf2MafScript = vcf2MafScript;
	}
}
