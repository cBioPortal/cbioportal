// package
package org.mskcc.cbio.importer.model;

// imports

/**
 * Class which contains directory metadata.
 */
public final class DirectoryMetadata {

	// bean properties
    private String stddataDownloadDirectory;
    private String analysisDownloadDirectory;
    private String referenceDataDirectory;
    private String gdacOverrideDirectory;
    private String privateOverrideDirectory;
    private String publicOverrideDirectory;

    /**
     * Create a DirectoryMetadata instance with specified properties.
     *
	 * @param stddataDownloadDirectory String
	 * @param analysisDownloadDirectory String
	 * @param referenceDataDirectory String
	 * @param gdacOverrideDirectory String
	 * @param privateOverrideDirectory String 
	 * @param publicOverrideDirectory String
     */
    public DirectoryMetadata(final String stddataDownloadDirectory, final String analysisDownloadDirectory,
                             final String referenceDataDirectory, final String gdacOverrideDirectory,
                             final String privateOverrideDirectory, final String publicOverrideDirectory) {

		if (stddataDownloadDirectory == null) {
            throw new IllegalArgumentException("stddataDownloadDirectory must not be null");
		}
		this.stddataDownloadDirectory = stddataDownloadDirectory;

		if (analysisDownloadDirectory == null) {
            throw new IllegalArgumentException("analysisDownloadDirectory must not be null");
		}
		this.analysisDownloadDirectory = analysisDownloadDirectory;

		if (referenceDataDirectory == null) {
            throw new IllegalArgumentException("referenceDataDirectory must not be null");
		}
		this.referenceDataDirectory = referenceDataDirectory;

		if (gdacOverrideDirectory == null) {
            throw new IllegalArgumentException("gdacOverrideDirectory must not be null");
		}
		this.gdacOverrideDirectory = gdacOverrideDirectory;

		if (privateOverrideDirectory == null) {
            throw new IllegalArgumentException("privateOverrideDirectory must not be null");
		}
		this.privateOverrideDirectory = privateOverrideDirectory;

		if (publicOverrideDirectory == null) {
            throw new IllegalArgumentException("publicOverrideDirectory must not be null");
		}
		this.publicOverrideDirectory = publicOverrideDirectory;
	}

	public String getSTDDATADownloadDirectory() { return stddataDownloadDirectory; }
	public String getAnalysisDownloadDirectory() { return analysisDownloadDirectory; }
	public String getReferenceDataDirectory() { return referenceDataDirectory; }
	public String getGDACOverrideDirectory() { return gdacOverrideDirectory; }
	public String getPrivateOverrideDirectory() { return privateOverrideDirectory; }
	public String getPublicOverrideDirectory() { return publicOverrideDirectory; }
}
