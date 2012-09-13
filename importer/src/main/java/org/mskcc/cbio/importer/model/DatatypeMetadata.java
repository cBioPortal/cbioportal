// package
package org.mskcc.cbio.importer.model;

// imports

/**
 * Class which contains datatype  metadata.
 */
public final class DatatypeMetadata {

	// Datatype enum
	public static enum DATATYPE {
		
		// types
		AGILENT_MRNA("AGILENT_MRNA"),
		CNA("CNA"),
		CNA_SEG("CNA_SEG"),
		LOG2CNA("LOG2CNA"),
		MUTATION("MUTATION"),
		RNA_SEQ("RNA_SEQ"),
		METHYLATION("METHYLATION"),
		CORRELATE_METHYLATION_VS_MRNA("CORRELATE_METHYLATION_VS_MRNA");

		//string ref for readable name
		private String datatype;
		
		// constructor
		DATATYPE(String datatype) { this.datatype = datatype; }

		// method toget enum readable name
		public String toString() { return datatype; }
	}

	// bean properties
	private DATATYPE datatype;
	private String packageFilename;
	private String dataFilename;
	private String overrideFilename;
	private Boolean download; // download?

    /**
     * Create a DatatypeMetadata instance with specified properties.
     *
	 * @param datatype DATATYPE
	 * @param packageFilename String
	 * @param dataFilename String
	 * @param overrideFilename String
	 * @param download Boolean
     */
    public DatatypeMetadata(final DATATYPE datatype, final Boolean download,
							final String packageFilename, final String dataFilename, final String overrideFilename) {

		if (datatype == null) {
            throw new IllegalArgumentException("datatype must not be null");
		}
		this.datatype = datatype;

		if (packageFilename == null) {
            throw new IllegalArgumentException("packageFilename must not be null");
		}
		this.packageFilename = packageFilename;

		if (dataFilename == null) {
            throw new IllegalArgumentException("dataFilename must not be null");
		}
		this.dataFilename = dataFilename;

		if (overrideFilename == null) {
			this.overrideFilename = "";
		}
		else {
			this.overrideFilename = overrideFilename;
		}

		if (download == null) {
            throw new IllegalArgumentException("download must not be null");
		}
		this.download = download;
	}

	public DATATYPE getDatatype() { return datatype; }
	public String getPackageFilename() { return packageFilename; }
	public String getDataFilename() { return dataFilename; }
	public String getOverrideFilename() { return overrideFilename; }
	public Boolean getDownload() { return download; }
}
