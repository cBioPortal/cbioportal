package org.mskcc.cbio.annotator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.maf.MafHeaderUtil;
import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Annotator service to get annotation data.
 * Currently, this class is implemented as a local service.
 * It gets the annotation information from a local source.
 *
 * @author Selcuk Onur Sumer
 */
public class AnnotatorService
{
	private static Log LOG = LogFactory.getLog(AnnotatorService.class);

	private BufferedReader reader;
	private MafUtil mafUtil;
	private String headerLine;
	private Map<String, String> lineCache;
	private AnnotatorConfig config;

	public AnnotatorService(AnnotatorConfig config)
	{
		try
		{
			this.config = config;
			this.lineCache = this.buildMap(config.getIntermediateMaf());
		}
		catch (IOException e)
		{
			this.reader = null;
			this.headerLine = null;
			this.mafUtil = null;
		}
	}

	private Map<String, String> buildMap(String filename) throws IOException
	{
		Map<String, String> cache = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		MafHeaderUtil headerUtil = new MafHeaderUtil();
		String headerLine = headerUtil.extractHeader(reader);
		MafUtil mafUtil = new MafUtil(headerLine);

		String line;

		while ((line = reader.readLine()) != null)
		{
			MafRecord record = mafUtil.parseRecord(line);

			cache.put(MafUtil.generateKey(record), line);
		}

		reader.close();

		this.headerLine = headerLine;
		this.mafUtil = mafUtil;

		return cache;
	}

	public Map<String, String> annotateRecord(MafRecord mafRecord) throws IOException
	{
		Map<String, String> data = new HashMap<String, String>();

		if (this.reader == null)
		{
			this.reader = new BufferedReader(
					new FileReader(this.config.getIntermediateMaf()));

			MafHeaderUtil headerUtil = new MafHeaderUtil();
			this.headerLine = headerUtil.extractHeader(this.reader);
			this.mafUtil = new MafUtil(headerLine);
		}

		// TODO make sure that this line actually corresponds to the given maf record...
		String line = this.reader.readLine();

		if (line == null)
		{
			LOG.warn("annotateRecordWithoutCache(), input vs intermediate file size mismatch.");
		}
		else
		{
			MafRecord record = this.mafUtil.parseRecord(line);
			String annotatedKey = MafUtil.generateKey(record);
			String originalKey = MafUtil.generateKey(mafRecord);


			if (!annotatedKey.equals(originalKey))
			{
				LOG.warn("annotateRecordWithoutCache(), key mismatch: " +
				         originalKey + " & " + annotatedKey);
			}

			for (String header: this.headerLine.split("\t"))
			{
				String parts[] = line.split("\t", -1);

				data.put(header, parts[this.mafUtil.getColumnIndex(header)]);
			}
		}

		return data;
	}

	public Map<String, String> annotateRecordWithCache(MafRecord mafRecord) throws IOException
	{
		Map<String, String> data = new HashMap<String, String>();

		String line = this.lineCache.get(MafUtil.generateKey(mafRecord));

		if (line == null)
		{
			LOG.warn("annotateRecord(), record cannot be found in the intermediate output file.");
		}
		else
		{
			for (String header: this.headerLine.split("\t"))
			{
				String parts[] = line.split("\t", -1);

				data.put(header, parts[this.mafUtil.getColumnIndex(header)]);
			}
		}

		return data;
	}

	public void cleanUp() throws IOException
	{
		if (this.reader != null)
		{
			this.reader.close();
		}

		// TODO also delete intermediate files?
	}
}
