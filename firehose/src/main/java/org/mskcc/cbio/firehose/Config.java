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
	 * @return String
	 */
	public String getLatestAnalysisRun();

	/**
	 * Sets the latest analysis run.
	 *
	 * @param String
	 */
	public void setLatestAnalysisRun(String latestAnalysisRun);

	/**
	 * Gets the latest STDDATA run.
	 *
	 * @return String
	 */
	public String getLatestSTDDATARun();

	/**
	 * Sets the latest STDDATA run.
	 *
	 * @param String
	 */
	public void setLatestSTDDATARun(String latestSTDDataRun);

	/**
	 * Gets the analysis datatypes to download from the firehose.
	 *
	 * @return String
	 */
	public String getAnalysisDatatypes();

	/**
	 * Gets the stddata datatypes to download from the firehose.
	 *
	 * @return String
	 */
	public String getSTDDATADatatypes();

	/**
	 * Gets the cancer studies to download from the firehose.
	 *
	 * @return String
	 */
	public String getCancerStudiesToDownload();
}
