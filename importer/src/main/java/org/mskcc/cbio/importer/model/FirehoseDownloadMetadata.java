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
