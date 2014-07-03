package org.mskcc.cbio.annotator;

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
	private BufferedReader reader;
	private MafUtil mafUtil;
	private String headerLine;

	public AnnotatorService()
	{
		try
		{
			// TODO make the input source configurable
			// (instead of using INTERMEDIATE_OUT_MAF)
			this.reader = new BufferedReader(
				new FileReader(Annotator.INTERMEDIATE_OUT_MAF));

			MafHeaderUtil headerUtil = new MafHeaderUtil();

			this.headerLine = headerUtil.extractHeader(reader);
			this.mafUtil = new MafUtil(headerLine);
		}
		catch (IOException e)
		{
			this.reader = null;
			this.headerLine = null;
			this.mafUtil = null;
		}
	}

	public Map<String, String> annotateRecord(MafRecord mafRecord) throws IOException
	{
		Map<String, String> data = new HashMap<String, String>();

		if (this.reader == null)
		{
			return data;
		}

		// TODO ideally we should generate a key from the mafRecord,
		// ..and retrieve annotator data by using the key

		String line = this.reader.readLine();

		// TODO make sure that this line actually corresponds to the given maf record...

		for (String header: this.headerLine.split("\t"))
		{
			String parts[] = line.split("\t", -1);

			data.put(header, parts[this.mafUtil.getColumnIndex(header)]);
		}

		if (line == null)
		{
			this.reader.close();
		}

		return data;
	}
}
