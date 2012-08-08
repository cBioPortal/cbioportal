// package
package org.mskcc.cbio.firehose;

// imports

/**
 * Interface used to get/set configuration properties.
 */
public interface Config {

	/**
	 * Gets the latest analysis run.
	 *
	 * Returns the date of the latest analysis run
	 * downloaded by the firehose converter as "MM/dd/yyyy"
	 *
	 * @return String
	 */
	String getLatestAnalysisRunDownloaded();

	/**
	 * Sets the latest analysis run downloaded date.  Argument
	 * should be of the form "MM/dd/yyyy".
	 *
	 * @param String
	 */
	void setLatestAnalysisRunDownloaded(String latestAnalysisRun);

	/**
	 * Gets the latest STDDATA run.
	 *
	 * Returns the date of the latest stddata run
	 * downloaded by the firehose converter as "MM/dd/yyyy"
	 *
	 * @return String
	 */
	String getLatestSTDDATARunDownloaded();

	/**
	 * Sets the latest stddata run downloaded date.  Argument
	 * should be of the form "MM/dd/yyyy".
	 *
	 * @param String
	 */
	void setLatestSTDDATARunDownloaded(String latestSTDDataRun);

	/**
	 * Gets the analysis datatypes to download from the firehose.
	 *
	 * Returns a string, space delimited, with each firehose_get task
	 * to download, like:
	 *
	 * "CopyNumber_Gistic2 CopyNumber_Preprocess Correlate_Methylation Mutation_Assessor"
	 *
	 * See the firehose_get tool for more information about tasks.
	 *
	 * @return String
	 */
	String getAnalysisDatatypes();

	/**
	 * Gets the stddata datatypes to download from the firehose.
	 *
	 * Returns a string, space delimited, with each firehose_get task
	 * to download, like:
	 *
	 * "Merge_methylation Merge_rnaseq__ Merge_transcriptome"
	 *
	 * See the firehose_get tool for more information about tasks.
	 *
	 * @return String
	 */
	String getSTDDATADatatypes();

	/**
	 * Gets the cancer studies to download from the firehose.
	 *
	 * Returns a string, space delimited, with each cancer study
	 * to download, like:
	 *
	 * "blca brca cesc coadread"
	 *
	 * @return String
	 */
	String getCancerStudiesToDownload();
}
