// package
package org.mskcc.cbio.importer;

// imports
import org.mskcc.cbio.importer.model.ImportData;
import org.mskcc.cbio.importer.model.PortalMetadata;

import javax.swing.JTable;

/**
 * Interface used to convert portal data.
 */
public interface StagingFileFactory {

	/**
	 * Returns the given file contents in a JTable.
	 *
     * @param portalMetadata PortalMetadata
	 * @param importData ImportData
	 * @param JTable
	 * @throws Exception
	 */
	void createStagingFile(final PortalMetadata portalMetadata, final ImportData importData, final JTable jtable) throws Exception;
}