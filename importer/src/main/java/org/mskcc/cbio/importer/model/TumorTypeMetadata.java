// package
package org.mskcc.cbio.importer.model;

// imports

/**
 * Class which contains cancer study metadata.
 */
public final class TumorTypeMetadata {

	public static final String TUMOR_TYPE_REGEX = "<CANCER>";

	// bean properties
	private String tumorTypeID;
	private String tumorTypeDescription;
	private Boolean download; // download?

    /**
     * Create a TumorTypeMetadata instance with specified properties.
     *
	 * @param tumorTypeID String
	 * @param tumorTypeDescription
	 * @param download Boolean
     */
    public TumorTypeMetadata(final String tumorTypeID,
							   final String tumorTypeDescription, final Boolean download) {

		if (tumorTypeID == null) {
            throw new IllegalArgumentException("tumorTypeID must not be null");
		}
		this.tumorTypeID = tumorTypeID;

		if (tumorTypeDescription == null) {
            throw new IllegalArgumentException("tumorTypeDescription must not be null");
		}
		this.tumorTypeDescription = tumorTypeDescription;

		if (download == null) {
            throw new IllegalArgumentException("download must not be null");
		}
		this.download = download;
	}

	public String getTumorTypeID() { return tumorTypeID; }
	public String getTumorTypeDescription() { return tumorTypeDescription; }
	public Boolean getDownload() { return download; }
}
