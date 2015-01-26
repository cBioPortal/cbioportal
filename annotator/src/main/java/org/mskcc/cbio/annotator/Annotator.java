package org.mskcc.cbio.annotator;

import com.google.common.base.Joiner;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.mskcc.cbio.maf.*;

import java.io.*;
import java.util.ArrayList;
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

	public int annotateFile(File input,
			File output) throws IOException
	{
		int retVal = -1;

		// script to run depends on the extension
		if (input.getName().toLowerCase().endsWith(".vcf"))
		{
			retVal = this.runVcf2Maf(input, output);
		}
		// assuming it is a maf..
		else
		{
			retVal = this.runMaf2Maf(input);

			if (retVal == 0)
			{
				// not using the original input anymore, it is not safe to merge line by line
				// annotator may change both the order of lines and the chromosome position...
				//mergeWithOriginal(input, output);

				// only keep the comment lines,
				// assuming that annotator handles everything else
				this.generateOutput(input, output);
			}
		}

		return retVal;
	}

	/**
	 * Generates the final output file.
	 *
	 * @param input     original input
	 * @param output    target output
	 * @throws IOException
	 */
	protected void generateOutput(File input, File output) throws IOException
	{
		BufferedReader bufReader = new BufferedReader(new FileReader(input));
		MafHeaderUtil headerUtil = new MafHeaderUtil();

		// this is to get comments from the original input
		headerUtil.extractHeader(bufReader);

		bufReader.close();

		FileWriter writer = new FileWriter(output);

		// write comments/metadata to the output
		FileIOUtil.writeLines(writer, headerUtil.getComments());

		bufReader = new BufferedReader(
				new FileReader(this.config.getIntermediateMaf()));


		// get everything else from the intermediate annotator output maf

		String line;

		while ((line = bufReader.readLine()) != null)
		{
			writer.write(line);
			writer.write("\n");
		}

		bufReader.close();
		writer.close();
	}

	/**
	 * Merges original input file with the annotator (intermediate) output file.
	 *
	 * @param input     original input
	 * @param output    target output
	 * @throws IOException
	 */
	protected void mergeWithOriginal(File input, File output) throws IOException
	{
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

		service.cleanUp();
		bufReader.close();
		writer.close();
	}

	public int runMaf2Maf(File input) throws IOException
	{
		String inputMaf = input.getAbsolutePath();

		CommandLine cmdLine = new CommandLine("perl");

		cmdLine.addArgument(this.config.getMaf2maf());
		cmdLine.addArgument("--vep-path");
		cmdLine.addArgument(this.config.getVepPath());
		cmdLine.addArgument("--vep-data");
		cmdLine.addArgument(this.config.getVepData());
		cmdLine.addArgument("--ref-fasta");
		cmdLine.addArgument(this.config.getRefFasta());
		cmdLine.addArgument("--retain-cols");
		cmdLine.addArgument(this.getRetainCols(input, this.config.getExcludeCols()));
		cmdLine.addArgument("--input-maf");
		cmdLine.addArgument(inputMaf);
		//cmdLine.addArgument("--output-dir");
		//cmdLine.addArgument(this.config.getIntermediateDir());
		cmdLine.addArgument("--output-maf");
		cmdLine.addArgument(this.config.getIntermediateMaf());
		cmdLine.addArgument("--tmp-dir");
		cmdLine.addArgument(this.config.getTmpDir());
		cmdLine.addArgument("--vep-forks");
		cmdLine.addArgument(this.config.getVepForks());

		return execProcess(cmdLine);
	}

	public int runVcf2Maf(File input, File output) throws IOException
	{
		String inVcf = input.getAbsolutePath();
		String outMaf = output.getAbsolutePath();

		CommandLine cmdLine = new CommandLine("perl");

		cmdLine.addArgument(this.config.getVcf2maf());
		cmdLine.addArgument("--vep-path");
		cmdLine.addArgument(this.config.getVepPath());
		cmdLine.addArgument("--vep-data");
		cmdLine.addArgument(this.config.getVepData());
		cmdLine.addArgument("--ref-fasta");
		cmdLine.addArgument(this.config.getRefFasta());
		cmdLine.addArgument("--input-vcf");
		cmdLine.addArgument(inVcf);
		cmdLine.addArgument("--output-maf");
		cmdLine.addArgument(outMaf);

		return execProcess(cmdLine);
	}

	/**
	 * Executes an external process via system call.
	 *
	 * @param cmdLine   process arguments (including the process itself)
	 * @return          exit value of the process
	 * @throws IOException  if an IO error occurs
	 */
	public static int execProcess(CommandLine cmdLine) throws IOException
	{
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

		//ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
		Executor executor = new DefaultExecutor();
		//executor.setExitValue(1);
		//executor.setWatchdog(watchdog);
		executor.execute(cmdLine, resultHandler);

		// wait for process to complete
		try
		{
			resultHandler.waitFor();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		return resultHandler.getExitValue();
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

	protected String getRetainCols(File input, String excludeCols) throws IOException
	{
		List<String> retainCols = new ArrayList<>();

		BufferedReader bufReader = new BufferedReader(new FileReader(input));
		MafHeaderUtil headerUtil = new MafHeaderUtil();
		String header = headerUtil.extractHeader(bufReader);

		bufReader.close();

		// headers in the original input file
		String[] cols = header.toLowerCase().split("\t");

		// headers to exclude from the output file
		String[] excluded = excludeCols.toLowerCase().split(",");

		// find out which columns to exclude
		for (String col: cols)
		{
			boolean exclude = false;

			for (String exCol: excluded)
			{
				// TODO startsWith may not be safe, do an exact search instead?
				if (col.startsWith(exCol))
				{
					exclude = true;
					break;
				}
			}

			if (!exclude)
			{
				retainCols.add(col);
			}
		}

		return Joiner.on(",").join(retainCols);
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
