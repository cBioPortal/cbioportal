// package
package org.mskcc.cbio.importer;

// imports
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;

import java.util.Collection;

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
	void setLatestAnalysisRunDownloaded(final String latestAnalysisRun);

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
	void setLatestSTDDATARunDownloaded(final String latestSTDDataRun);

	/**
	 * Gets a collection of CancerStudyMetadata.
	 *
	 * @return Collection<CancerStudyMetadata>
	 */
	Collection<CancerStudyMetadata> getCancerStudyMetadata();

	/**
	 * Gets a collection of DatatypeMetadata.
	 *
	 * @return Collection<DatatypeMetadata>
	 */
	Collection<DatatypeMetadata> getDatatypeMetadata();
}
