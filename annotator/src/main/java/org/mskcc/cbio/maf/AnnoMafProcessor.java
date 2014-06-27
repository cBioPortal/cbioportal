package org.mskcc.cbio.maf;


import java.util.ArrayList;
import java.util.List;

public class AnnoMafProcessor extends MafProcessor
{
	protected List<String> annoHeaders;

	public AnnoMafProcessor(String headerLine, List<String> annoHeaders)
	{
		super(headerLine);
		//this.annoHeaders = this.initAnnoHeaderList();
		this.annoHeaders = annoHeaders;
	}

	/**
	 * Adds oncotator column names into the given header list.
	 *
	 * @param headerData    list of header names
	 * @param addMissing    indicates if the missing columns will also be added
	 */
	protected void addOncoColsToHeader(List<String> headerData,
			boolean addMissing)
	{
		for (String header : this.oncoHeaders)
		{
			// never add missing oncotator columns
			this.addColumnToHeader(headerData, header, false);
		}
	}

	/**
	 * Adds MA column names into the given header list.
	 *
	 * @param headerData    list of header names
	 * @param addMissing    indicates if the missing columns will also be added
	 */
	protected void addMaColsToHeader(List<String> headerData,
			boolean addMissing)
	{
		for (String header : this.maHeaders)
		{
			// never add missing MA columns
			this.addColumnToHeader(headerData, header, false);
		}
	}

	/**
	 * Adds new annotator column names into the header list.
	 *
	 * @param headerData    list of header names
	 */
	protected void addNewColsToHeader(List<String> headerData)
	{
		for (String header : this.annoHeaders)
		{
			if (!headerData.contains(header))
			{
				// add missing annotator headers
				headerData.add(header);
			}
		}
	}

	/**
	 * Updates the data list by adding annotator data.
	 *
	 * @param data              data representing a single line in the input
	 */
	public void updateAnnoData(List<String> data/*, TODO AnnoRecord? record*/)
	{
		// TODO update the data using the annotator data
	}

	/**
	 * Initializes the Annotator header list.
	 * The order of the elements in this list is directly related
	 * to the order of the columns in the output file.
	 *
	 * @return  a list of annotator column names
	 */
	protected List<String> initAnnoHeaderList()
	{
		List<String> headers = new ArrayList<String>();

		// TODO set columns names here...

		return headers;
	}
}
