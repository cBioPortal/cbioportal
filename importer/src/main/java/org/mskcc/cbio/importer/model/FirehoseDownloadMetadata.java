/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

// package
package org.mskcc.cbio.importer.model;

// imports

/**
 * Class which contains firehose download metadata.
 */
public final class FirehoseDownloadMetadata {

	// bean properties
    private String stddataDownloadDirectory;
    private String analysisDownloadDirectory;
    private String latestSTDDATARunDownloaded;
    private String latestAnalysisRunDownloaded;

    /**
     * Create a FirehoseDownloadMetadata instance with specified properties.
     *
	 * @param analysisDownloadDirectory String
     * @param latestAnalysisRunDownloaded String
	 * @param stddataDownloadDirectory String
     * @param latestSTDDATARunDownloaded String
     */
    public FirehoseDownloadMetadata(final String analysisDownloadDirectory, final String latestAnalysisRunDownloaded,
                                    final String stddataDownloadDirectory, final String latestSTDDATARunDownloaded) {

		if (analysisDownloadDirectory == null) {
            throw new IllegalArgumentException("analysisDownloadDirectory must not be null");
		}
		this.analysisDownloadDirectory = analysisDownloadDirectory;

		if (latestAnalysisRunDownloaded == null) {
            throw new IllegalArgumentException("latestAnalysisRunDownloaded must not be null");
		}
		this.latestAnalysisRunDownloaded = latestAnalysisRunDownloaded;

		if (stddataDownloadDirectory == null) {
            throw new IllegalArgumentException("stddataDownloadDirectory must not be null");
		}
		this.stddataDownloadDirectory = stddataDownloadDirectory;

		if (latestSTDDATARunDownloaded == null) {
            throw new IllegalArgumentException("latestSTDDATARunDownloaded must not be null");
		}
		this.latestSTDDATARunDownloaded = latestSTDDATARunDownloaded;
	}

	public String getSTDDATADownloadDirectory() { return stddataDownloadDirectory; }
	public String getAnalysisDownloadDirectory() { return analysisDownloadDirectory; }

	public String getLatestSTDDATARunDownloaded() { return latestSTDDATARunDownloaded; }
	public void setLatestSTDDATARunDownloaded(final String latestSTDDATARunDownloaded) { this.latestSTDDATARunDownloaded = latestSTDDATARunDownloaded; }

	public String getLatestAnalysisRunDownloaded() { return latestAnalysisRunDownloaded; }
	public void setLatestAnalysisRunDownloaded(final String latestAnalysisRunDownloaded) { this.latestAnalysisRunDownloaded = latestAnalysisRunDownloaded; }
}
