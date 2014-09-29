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
	private AnnotatorConfig config;

	public Annotator(AnnotatorConfig config)
	{
		// init default settings
		this.config = config;
	}

	public void annotateFile(File input,
			File output) throws IOException
	{
		int retVal = -1;

		// script to run depends on the extension
		if (input.getName().toLowerCase().endsWith(".vcf"))
		{
			retVal = this.runVcf2Maf(input, output);
			return;
		}
		// assuming it is a maf..
		else
		{
			retVal = this.runMaf2Maf(input);
		}

		// TODO check return value?

		List<String> annoHeaders = this.extractAnnoHeaders(this.config.getIntermediateMaf());

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
				this.config.isSort(), this.config.isAddMissing());

		// write the header line to output
		FileIOUtil.writeLine(writer, columnNames);

		String dataLine = bufReader.readLine();
		AnnotatorService service = new AnnotatorService(this.config);

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

			// get the data and update/add new annotator columns
			List<String> data = processor.newDataList(dataLine);

			processor.updateAnnoData(data, annoData);

			// write data to the output file
			FileIOUtil.writeLine(writer, data);

			dataLine = bufReader.readLine();
		}

		bufReader.close();
		writer.close();

	}

	public int runMaf2Maf(File input) throws IOException
	{
		String inputMaf = input.getAbsolutePath();

		String[] args = {
			"perl",
			this.config.getMaf2maf(),
			"--vep-path",
			this.config.getVepPath(),
			"--vep-data",
			this.config.getVepData(),
			"--input-maf",
			inputMaf,
			//"--output-dir",
			//this.config.getIntermediateDir(),
			"--output-maf",
			this.config.getIntermediateMaf()
		};

		return execProcess(args);
	}

	public int runVcf2Maf(File input, File output) throws IOException
	{
		String inVcf = input.getAbsolutePath();
		String outMaf = output.getAbsolutePath();

		String[] args = {
			"perl",
			this.config.getVcf2maf(),
			"--vep-path",
			this.config.getVepPath(),
			"--vep-data",
			this.config.getVepData(),
			"--input-vcf",
			inVcf,
			"--output-maf",
			outMaf
		};

		return execProcess(args);
	}

	// TODO code duplication! -- we have the same code in liftover module
	// also, this is not always safe if there are too many error messages
	// both buffers should be emptied at the same time (using threads)
	/**
	 * Executes an external process via system call.
	 *
	 * @param args          process arguments (including the process itself)
	 * @return              exit value of the process
	 * @throws java.io.IOException  if an IO error occurs
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

	public AnnotatorConfig getConfig()
	{
		return config;
	}

	public void setConfig(AnnotatorConfig config)
	{
		this.config = config;
	}
}
