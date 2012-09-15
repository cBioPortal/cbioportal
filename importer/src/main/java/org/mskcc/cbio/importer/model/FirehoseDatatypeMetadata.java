// package
package org.mskcc.cbio.importer.model;

/**
 * Class which contains firehose datatype metadata.
 */
public final class FirehoseDatatypeMetadata {

	// bean properties
	private String archive;
	private String datatype;
	private Boolean download;
	private String dataFilename;

    /**
     * Create a FirehoseDatatypeMetadata instance with specified properties.
     *
	 * @param datatype String
	 * @param download Boolean
	 * @param archive String
	 * @param dataFilename String
     */
    public FirehoseDatatypeMetadata(final String datatype, final Boolean download,
                                    final String archive, final String dataFilename) {

		if (datatype == null) {
            throw new IllegalArgumentException("datatype must not be null");
		}
		this.datatype = datatype;

		if (archive == null) {
            throw new IllegalArgumentException("archive must not be null");
		}
		this.archive = archive;

		if (dataFilename == null) {
            throw new IllegalArgumentException("dataFilename must not be null");
		}
		this.dataFilename = dataFilename;

		if (download == null) {
            throw new IllegalArgumentException("download must not be null");
		}
		this.download = download;
	}

	public String getDatatype() { return datatype; }
	public String getArchiveFilename() { return archive; }
	public String getDataFilename() { return dataFilename; }
	public Boolean getDownload() { return download; }
}
