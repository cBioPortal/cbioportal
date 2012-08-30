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
	private String portals; // portal to install

    /**
     * Create a CancerStudyMetadata instance with specified properties.
     *
	 * @param cancerStudyID String
	 * @param cancerStudyDescription
	 * @param download Boolean
	 * @param portal String
     */
    public CancerStudyMetadata(final String cancerStudyID, final String cancerStudyDescription,
					  final Boolean download, final String portals) {

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

		if (portals == null) {
            throw new IllegalArgumentException("portals must not be null");
		}
		this.portals = portals;
	}

	public String getCancerStudyID() { return cancerStudyID; }
	public String getCancerStudyDescription() { return cancerStudyDescription; }
	public Boolean getDownload() { return download; }
	public String getPortals() { return portals; }
}
