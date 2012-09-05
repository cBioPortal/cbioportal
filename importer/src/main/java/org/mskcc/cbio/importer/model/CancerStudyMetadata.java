// package
package org.mskcc.cbio.importer.model;

// imports

/**
 * Class which contains cancer study metadata.
 */
public final class CancerStudyMetadata {

	// bean properties
	private String cancerStudyID;
	private String cancerStudyDescription;
	private Boolean download; // download?

    /**
     * Create a CancerStudyMetadata instance with specified properties.
     *
	 * @param cancerStudyID String
	 * @param cancerStudyDescription
	 * @param download Boolean
     */
    public CancerStudyMetadata(final String cancerStudyID,
							   final String cancerStudyDescription, final Boolean download) {

		if (cancerStudyID == null) {
            throw new IllegalArgumentException("cancerStudyID must not be null");
		}
		this.cancerStudyID = cancerStudyID;

		if (cancerStudyDescription == null) {
            throw new IllegalArgumentException("cancerStudyDescription must not be null");
		}
		this.cancerStudyDescription = cancerStudyDescription;

		if (download == null) {
            throw new IllegalArgumentException("download must not be null");
		}
		this.download = download;
	}

	public String getCancerStudyID() { return cancerStudyID; }
	public String getCancerStudyDescription() { return cancerStudyDescription; }
	public Boolean getDownload() { return download; }
}
