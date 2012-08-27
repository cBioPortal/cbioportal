// package
package org.mskcc.cbio.importer;

// imports

/**
 * Interface used to get/set configuration properties.
 */
public interface Config {

	/**
	 * Gets the latest analysis run.
	 *
	 * Returns the date of the latest analysis run
	 * processed by the importer as "MM/dd/yyyy"
	 *
	 * @return String
	 */
	String getLatestAnalysisRunDownloaded();

	/**
	 * Sets the latest analysis run processed by the importer.  Argument
	 * should be of the form "MM/dd/yyyy".
	 *
	 * @param String
	 */
	void setLatestAnalysisRunDownloaded(String latestAnalysisRun);

	/**
	 * Gets the latest STDDATA run.
	 *
	 * Returns the date of the latest stddata run
	 * downloaded by the importer as "MM/dd/yyyy"
	 *
	 * @return String
	 */
	String getLatestSTDDATARunDownloaded();

	/**
	 * Sets the latest stddata run processed by the importer.  Argument
	 * should be of the form "MM/dd/yyyy".
	 *
	 * @param String
	 */
	void setLatestSTDDATARunDownloaded(String latestSTDDataRun);

	/**
	 * Gets the analysis datatypes to process.
	 *
	 * Returns a string, space delimited, with each type to download, like:
	 *
	 * "CopyNumber_Gistic2 CopyNumber_Preprocess Correlate_Methylation Mutation_Assessor"
	 *
	 * @return String
	 */
	String getAnalysisDatatypes();

	/**
	 * Gets the stddata datatypes to process.
	 *
	 * Returns a string, space delimited, with each type to download, like:
	 *
	 * "Merge_methylation Merge_rnaseq__ Merge_transcriptome"
	 *
	 * @return String
	 */
	String getSTDDATADatatypes();

	/**
	 * Gets the cancer studies to process.
	 *
	 * Returns a string, space delimited, with each cancer study
	 * to process, like:
	 *
	 * "blca brca cesc coadread"
	 *
	 * @return String
	 */
	String getCancerStudiesToDownload();
}
